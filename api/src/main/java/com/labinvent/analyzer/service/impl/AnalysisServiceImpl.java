package com.labinvent.analyzer.service.impl;

import com.labinvent.analyzer.dto.AnalysisDetailDto;
import com.labinvent.analyzer.dto.HistoryItemDto;
import com.labinvent.analyzer.entity.AnalysisResult;
import com.labinvent.analyzer.entity.AnalysisResultStatus;
import com.labinvent.analyzer.exception.NotFoundException;
import com.labinvent.analyzer.mapper.AnalysisMapper;
import com.labinvent.analyzer.repository.AnalysisResultRepository;
import com.labinvent.analyzer.service.AnalysisService;
import com.labinvent.analyzer.service.executor.AnalysisExecutor;
import com.labinvent.analyzer.service.progress.ProgressRegistry;
import com.labinvent.analyzer.service.progress.ProgressState;
import com.labinvent.analyzer.service.StorageService;
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

    private final AnalysisResultRepository repository;
    private final StorageService storageService;
    private final ProgressRegistry progressRegistry;
    private final AnalysisMapper mapper;
    private final AnalysisExecutor executor;

    @Override
    @Transactional
    public void registerFile(String fileName, long size, String path) {
        AnalysisResult result = AnalysisResult.builder()
                .fileName(fileName)
                .fileSizeBytes(size)
                .tempFilePath(path)
                .uploadedAt(Instant.now())
                .status(AnalysisResultStatus.UPLOADED)
                .build();

        result = repository.save(result);
        enforceHistoryLimit();
        log.info("Файл [{}] зарегистрирован с id={}", fileName, result.getId());
    }

    @Override
    @Transactional
    public void startAnalysis(Long id) {
        AnalysisResult result = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Файл не найден"));

        startAnalysis(result);
    }

    @Override
    public void startAnalysis(AnalysisResult result) {

        if (!result.getStatus().canStart()) {
            log.warn("Анализ id={} не может быть запущен, статус={}", result.getId(), result.getStatus());
            return;
        }

        ProgressState state = progressRegistry.getOrCreate(result.getId());

        executor.runAnalysis(result, state);
        log.info("Анализ файла id={} запущен", result.getId());
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
        AnalysisResult result = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Запись не найдена"));
        storageService.deleteTempFile(result.getTempFilePath());
        repository.delete(result);
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
            List<AnalysisResult> oldest = repository.findOldest(PageRequest.of(0, toDelete));
            //todo если java21, то переписать через Files.delete(Path[])
            oldest.forEach(old -> storageService.deleteTempFile(old.getTempFilePath()));
            repository.deleteAllInBatch(oldest);
            log.info("Удалено {} старых записей анализа", toDelete);
        }
    }
}