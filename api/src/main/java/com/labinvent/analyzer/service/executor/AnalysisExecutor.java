package com.labinvent.analyzer.service.executor;

import com.labinvent.analyzer.entity.AnalysisMetrics;
import com.labinvent.analyzer.entity.AnalysisResult;
import com.labinvent.analyzer.entity.AnalysisResultStatus;
import com.labinvent.analyzer.repository.AnalysisResultRepository;
import com.labinvent.analyzer.service.notify.AnalysisNotifier;
import com.labinvent.analyzer.service.progress.ProgressRegistry;
import com.labinvent.analyzer.service.progress.ProgressState;
import com.labinvent.analyzer.util.ReaderBundle;
import com.labinvent.analyzer.util.StatsAccumulator;
import com.labinvent.analyzer.util.impl.CsvFileProcessorImpl;
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

    private final AnalysisResultRepository repository;
    private final ProgressRegistry progressRegistry;
    private final AnalysisNotifier notifier;
    private final CsvFileProcessorImpl processor;

    @Async("analysisExecutor")
    public void runAnalysis(Long id, ProgressState state, String path) {
        long startMillis = System.currentTimeMillis();
        try {
            AnalysisResult record = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Запись анализа не найдена"));

            Path filePath = Paths.get(path);
            long totalBytes = Files.size(filePath);

            try (ReaderBundle bundle = processor.openReader(filePath)) {
                BufferedReader br = bundle.reader();
                StatsAccumulator acc = new StatsAccumulator();
                String line;
                long lineNo = 0;
                int lastPercent = 0;

                notifier.notifyStatus(id, AnalysisResultStatus.PROCESSING.name(), 0);

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
            notifier.notifyStatus(id, AnalysisResultStatus.PROCESSING.name(), percent);
            return percent;
        }
        return lastPercent;
    }

    private void finalizeRecord(AnalysisResult record, StatsAccumulator acc, long startMillis) {
        AnalysisMetrics metrics = AnalysisMetrics.builder()
                .count(acc.getCount())
                .minValue(acc.getMin())
                .maxValue(acc.getMax())
                .avg(acc.getMean())
                .stdDev(acc.getStdDev())
                .skipCount(acc.getInvalidCount())
                .uniqueCount(acc.getUniqueCount())
                .build();

        record.setMetrics(metrics);
        record.setStatus(AnalysisResultStatus.DONE);
        record.setProcessedAt(Instant.now());
        record.setProcessDurationMillis(System.currentTimeMillis() - startMillis);

        repository.save(record);
        notifier.notifyStatus(record.getId(), AnalysisResultStatus.DONE.name(), 100);
        progressRegistry.remove(record.getId());
    }

    private void markCancelled(AnalysisResult record, Long id, ProgressState state) {
        record.setStatus(AnalysisResultStatus.CANCELLED);
        repository.save(record);
        notifier.notifyStatus(id, AnalysisResultStatus.CANCELLED.name(), state.getProgress());
        progressRegistry.remove(id);
    }

    private void markFailed(Long id, ProgressState state, long startMillis, Exception e) {
        log.error("Ошибка анализа id={}: {}", id, e.getMessage(), e);
        repository.findById(id).ifPresent(r -> {
            r.setStatus(AnalysisResultStatus.FAILED);
            r.setProcessedAt(Instant.now());
            r.setProcessDurationMillis(System.currentTimeMillis() - startMillis);
            repository.save(r);
            notifier.notifyStatus(id, AnalysisResultStatus.FAILED.name(), state.getProgress());
        });
        progressRegistry.remove(id);
    }
}
