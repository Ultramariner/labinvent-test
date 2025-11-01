package com.labinvent.analyzer.service.analysis.impl;

import com.labinvent.analyzer.dto.AnalysisDetailDto;
import com.labinvent.analyzer.dto.HistoryItemDto;
import com.labinvent.analyzer.entity.AnalysisRecord;
import com.labinvent.analyzer.entity.AnalysisRecordStatus;
import com.labinvent.analyzer.exception.NotFoundException;
import com.labinvent.analyzer.mapper.AnalysisMapper;
import com.labinvent.analyzer.repository.AnalysisRecordRepository;
import com.labinvent.analyzer.service.analysis.AnalysisService;
import com.labinvent.analyzer.service.analysis.notify.AnalysisNotifier;
import com.labinvent.analyzer.service.analysis.progress.ProgressRegistry;
import com.labinvent.analyzer.service.analysis.progress.ProgressState;
import com.labinvent.analyzer.service.storage.StorageService;
import com.labinvent.analyzer.util.CountingInputStream;
import com.labinvent.analyzer.util.ReaderBundle;
import com.labinvent.analyzer.util.StatsAccumulator;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class AnalysisServiceImpl implements AnalysisService {

    private final AnalysisRecordRepository repository;
    private final StorageService storageService;
    private final ProgressRegistry progressRegistry;
    private final AnalysisMapper mapper;

    private final AnalysisNotifier notifier;

    public AnalysisServiceImpl(AnalysisRecordRepository repository,
                               StorageService storageService,
                               ProgressRegistry progressRegistry,
                               AnalysisMapper mapper, AnalysisNotifier notifier) {
        this.repository = repository;
        this.storageService = storageService;
        this.progressRegistry = progressRegistry;
        this.mapper = mapper;
        this.notifier = notifier;
    }

    @Override
    @Transactional
    public Long registerFile(String fileName, long size, String path) {
        AnalysisRecord record = AnalysisRecord.builder()
                .fileName(fileName)
                .fileSizeBytes(size)
                .tempFilePath(path)
                .uploadedAt(Instant.now())
                .status(AnalysisRecordStatus.UPLOADED)
                .build();

        record = repository.save(record);
        enforceHistoryLimit();
        log.info("Файл [{}] зарегистрирован с id={}", fileName, record.getId());
        return record.getId();
    }

    @Override
    @Transactional
    public void startAnalysis(Long id) {
        AnalysisRecord record = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Файл не найден"));

        if (record.getStatus() != AnalysisRecordStatus.UPLOADED) {
            log.warn("Анализ id={} не может быть запущен, статус={}", id, record.getStatus());
            return;
        }

        record.setStatus(AnalysisRecordStatus.PROCESSING);
        repository.save(record);

        ProgressState state = progressRegistry.getOrCreate(record.getId());

        notifier.notifyStatus(record.getId(), AnalysisRecordStatus.PROCESSING.name(), 0);

        runAnalysis(record.getId(), state, record.getTempFilePath());
        log.info("Анализ файла id={} запущен", id);
    }

    //todo @Async <-> CompletableFuture.runAsync
    //todo TaskExecutor
    @Async
    public void runAnalysis(Long id, ProgressState state, String path) {
        long startMillis = System.currentTimeMillis();
        try {
            AnalysisRecord record = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Запись анализа не найдена"));

            Path filePath = Paths.get(path);
            long totalBytes = Files.size(filePath);

            try (ReaderBundle bundle = openReader(filePath)) {
                BufferedReader br = bundle.reader();

                StatsAccumulator acc = new StatsAccumulator();
                String line;
                long lineNo = 0;

                notifier.notifyStatus(id, AnalysisRecordStatus.PROCESSING.name(), 0);

                int lastPercent = 0;

                while ((line = br.readLine()) != null) {
                    lineNo++;
                    processLine(line, lineNo, acc);

                    lastPercent = updateProgressIfChanged(id, state, bundle, totalBytes, lastPercent);

                    if (state.isCancelled()) {
                        markCancelled(record, id, state);
                        return;
                    }
                }

                finalizeRecord(record, acc, startMillis);
            }
        } catch (Exception e) {
            markFailed(id, state, startMillis, e);
        }
    }

    private ReaderBundle openReader(Path filePath) throws IOException {
        InputStream is = Files.newInputStream(filePath);
        BufferedInputStream bis = new BufferedInputStream(is, 256 * 1024);
        CountingInputStream counting = new CountingInputStream(bis);
        Reader reader = new InputStreamReader(counting);
        BufferedReader br = new BufferedReader(reader, 256 * 1024);
        return new ReaderBundle(br, counting);
    }

    private void processLine(String line, long lineNo, StatsAccumulator acc) {
        if (lineNo == 1) return;
        if (line.isBlank()) { acc.addInvalid(); return; }

        int commaIdx = line.indexOf(',');
        if (commaIdx < 0) { acc.addInvalid(); return; }

        String valPart = line.substring(commaIdx + 1).trim();
        if (valPart.isEmpty()) { acc.addInvalid(); return; }

        try {
            double value = Double.parseDouble(valPart);
            if (!Double.isFinite(value)) { acc.addInvalid(); return; }
            acc.addValid(value);
        } catch (NumberFormatException e) {
            acc.addInvalid();
        }
    }

    private int updateProgressIfChanged(Long id, ProgressState state, ReaderBundle bundle, long totalBytes, int lastPercent) {
        long readBytes = bundle.getReadBytes();
        int percent = (int) Math.min(100, (readBytes * 100.0 / totalBytes));
        if (percent > lastPercent) {
            state.setProgress(percent);
            notifier.notifyStatus(id, AnalysisRecordStatus.PROCESSING.name(), percent);
            return percent;
        }
        return lastPercent;
    }

    private void finalizeRecord(AnalysisRecord record, StatsAccumulator acc, long startMillis) {
        record.setCount(acc.getCount());
        record.setMinValue(acc.getMin());
        record.setMaxValue(acc.getMax());
        record.setAvg(acc.getMean());
        record.setStdDev(acc.getStdDev());
        record.setSkipCount(acc.getInvalidCount());
        record.setUniqueCount(acc.getUniqueCount());
        record.setStatus(AnalysisRecordStatus.DONE);
        record.setProcessedAt(Instant.now());
        record.setProcessDurationMillis(System.currentTimeMillis() - startMillis);

        repository.save(record);
        notifier.notifyStatus(record.getId(), AnalysisRecordStatus.DONE.name(), 100);
        progressRegistry.remove(record.getId());
    }

    private void markCancelled(AnalysisRecord record, Long id, ProgressState state) {
        record.setStatus(AnalysisRecordStatus.CANCELLED);
        repository.save(record);
        notifier.notifyStatus(id, AnalysisRecordStatus.CANCELLED.name(), state.getProgress());
        progressRegistry.remove(id);
    }

    private void markFailed(Long id, ProgressState state, long startMillis, Exception e) {
        log.error("Ошибка анализа id={}: {}", id, e.getMessage(), e);
        repository.findById(id).ifPresent(r -> {
            r.setStatus(AnalysisRecordStatus.FAILED);
            r.setProcessedAt(Instant.now());
            r.setProcessDurationMillis(System.currentTimeMillis() - startMillis);
            repository.save(r);
            notifier.notifyStatus(id, AnalysisRecordStatus.FAILED.name(), state.getProgress());
        });
        progressRegistry.remove(id);
    }

        @Override
    public List<HistoryItemDto> getHistory() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "uploadedAt"));
        return repository.findAll(pageable).stream()
                .map(mapper::toHistoryItem)
                .toList();
    }

    @Override
    public AnalysisDetailDto getDetail(Long id) {
        return repository.findById(id)
                .map(mapper::toDetail)
                .orElseThrow(() -> new RuntimeException("Запись не найдена"));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        AnalysisRecord record = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Запись не найдена"));
        storageService.deleteTempFile(record.getTempFilePath());
        repository.delete(record);
        //todo убрать после переноса в метод анализа
        progressRegistry.remove(id);
        log.info("Анализ id={} удалён", id);
    }

    @Override
    public void cancel(Long id) {
        ProgressState state = progressRegistry.get(id);
        if (state != null) {
            state.cancel();
            log.info("Отмена анализа id={}", id);
        }
    }

    @Override
    public int getProgress(Long id) {
        ProgressState state = progressRegistry.get(id);
        if (state == null) {
            throw new NotFoundException("Прогресс анализа с id=" + id + " не найден");
        }
        return state.getProgress();
    }

    /**
     * Так как требуется хранить, а не отображать лишь 10 записей
     */
    @Transactional
    public void enforceHistoryLimit() {
        long count = repository.count();
        if (count > 10) {
            int toDelete = (int) (count - 10);
            List<AnalysisRecord> oldest = repository.findOldest(PageRequest.of(0, toDelete));
            //todo если java21, то переписать через Files.delete(Path[])
            oldest.forEach(old -> storageService.deleteTempFile(old.getTempFilePath()));
            repository.deleteAllInBatch(oldest);
            log.info("Удалено {} старых записей анализа", toDelete);
        }
    }
}