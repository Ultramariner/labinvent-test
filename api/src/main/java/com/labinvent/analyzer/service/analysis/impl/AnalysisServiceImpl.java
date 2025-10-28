package com.labinvent.analyzer.service.analysis.impl;

import com.labinvent.analyzer.dto.AnalysisDetailDto;
import com.labinvent.analyzer.dto.HistoryItemDto;
import com.labinvent.analyzer.entity.AnalysisRecord;
import com.labinvent.analyzer.entity.AnalysisRecordStatus;
import com.labinvent.analyzer.exception.NotFoundException;
import com.labinvent.analyzer.mapper.AnalysisMapper;
import com.labinvent.analyzer.repository.AnalysisRecordRepository;
import com.labinvent.analyzer.service.analysis.AnalysisService;
import com.labinvent.analyzer.service.analysis.progress.ProgressRegistry;
import com.labinvent.analyzer.service.analysis.progress.ProgressState;
import com.labinvent.analyzer.service.storage.StorageService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class AnalysisServiceImpl implements AnalysisService {

    private final AnalysisRecordRepository repository;
    private final StorageService storageService;
    private final ProgressRegistry progressRegistry;
    private final AnalysisMapper mapper;

    public AnalysisServiceImpl(AnalysisRecordRepository repository,
                               StorageService storageService,
                               ProgressRegistry progressRegistry,
                               AnalysisMapper mapper) {
        this.repository = repository;
        this.storageService = storageService;
        this.progressRegistry = progressRegistry;
        this.mapper = mapper;
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

        record.setStatus(AnalysisRecordStatus.PROCESSING);
        repository.save(record);

        ProgressState state = progressRegistry.getOrCreate(record.getId());
        runFakeAnalysis(record.getId(), state, record.getTempFilePath());
        log.info("Анализ файла id={} запущен", id);
    }

    //todo добавить логику анализа
    //todo @Async <-> CompletableFuture.runAsync
    //todo TaskExecutor
    @Async
    public void runFakeAnalysis(Long id, ProgressState state, String path) {
        try {
            for (int i = 1; i <= 100; i++) {
                if (state.isCancelled()) {
                    repository.findById(id).ifPresent(r -> {
                        r.setStatus(AnalysisRecordStatus.CANCELLED);
                        repository.save(r);
                    });
                    progressRegistry.remove(id);
                    return;
                }
                state.setProgress(i);
                Thread.sleep(50);
            }
            repository.findById(id).ifPresent(r -> {
                r.setStatus(AnalysisRecordStatus.DONE);
                r.setProcessedAt(Instant.now());
                r.setProcessDurationMillis(
                        Duration.between(r.getUploadedAt(), r.getProcessedAt()).toMillis()
                );
                repository.save(r);
            });
            progressRegistry.remove(id);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            repository.findById(id).ifPresent(r -> {
                r.setStatus(AnalysisRecordStatus.FAILED);
                repository.save(r);
            });
            progressRegistry.remove(id);
        }
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