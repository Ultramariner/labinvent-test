package com.labinvent.analyzer.service.analysis.impl;

import com.labinvent.analyzer.dto.AnalysisDetailDto;
import com.labinvent.analyzer.dto.HistoryItemDto;
import com.labinvent.analyzer.entity.AnalysisRecord;
import com.labinvent.analyzer.entity.AnalysisRecordStatus;
import com.labinvent.analyzer.exception.NotFoundException;
import com.labinvent.analyzer.mapper.AnalysisMapper;
import com.labinvent.analyzer.repository.AnalysisRecordRepository;
import com.labinvent.analyzer.service.analysis.AnalysisService;
import com.labinvent.analyzer.service.analysis.executor.AnalysisExecutor;
import com.labinvent.analyzer.service.analysis.notify.AnalysisNotifier;
import com.labinvent.analyzer.service.analysis.progress.ProgressRegistry;
import com.labinvent.analyzer.service.analysis.progress.ProgressState;
import com.labinvent.analyzer.service.storage.StorageService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class AnalysisServiceImpl implements AnalysisService {

    private final AnalysisRecordRepository repository;
    private final StorageService storageService;
    private final ProgressRegistry progressRegistry;
    private final AnalysisMapper mapper;
    private final AnalysisNotifier notifier;
    private final AnalysisExecutor executor;

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

        executor.runAnalysis(record.getId(), state, record.getTempFilePath());
        log.info("Анализ файла id={} запущен", id);
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