package com.labinvent.analyzer.service.analysis.executor;

import com.labinvent.analyzer.entity.AnalysisRecord;
import com.labinvent.analyzer.entity.AnalysisRecordStatus;
import com.labinvent.analyzer.repository.AnalysisRecordRepository;
import com.labinvent.analyzer.service.analysis.notify.AnalysisNotifier;
import com.labinvent.analyzer.service.analysis.progress.ProgressRegistry;
import com.labinvent.analyzer.service.analysis.progress.ProgressState;
import com.labinvent.analyzer.util.ReaderBundle;
import com.labinvent.analyzer.util.StatsAccumulator;
import com.labinvent.analyzer.util.impl.CsvFileProcessor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

@Slf4j
@Service
@AllArgsConstructor
public class AnalysisExecutor {

    private final AnalysisRecordRepository repository;
    private final ProgressRegistry progressRegistry;
    private final AnalysisNotifier notifier;
    private final CsvFileProcessor processor;

    //todo @Async <-> CompletableFuture.runAsync
    //todo TaskExecutor
    @Async("analysisExecutor")
    public void runAnalysis(Long id, ProgressState state, String path) {
        long startMillis = System.currentTimeMillis();
        try {
            AnalysisRecord record = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Запись анализа не найдена"));

            Path filePath = Paths.get(path);
            long totalBytes = Files.size(filePath);

            try (ReaderBundle bundle = processor.openReader(filePath)) {
                BufferedReader br = bundle.reader();
                StatsAccumulator acc = new StatsAccumulator();
                String line;
                long lineNo = 0;
                int lastPercent = 0;

                notifier.notifyStatus(id, AnalysisRecordStatus.PROCESSING.name(), 0);

                while ((line = br.readLine()) != null) {
                    lineNo++;
                    processor.processLine(line, lineNo, acc);

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
}
