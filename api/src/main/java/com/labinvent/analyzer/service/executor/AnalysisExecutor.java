package com.labinvent.analyzer.service.executor;

import com.labinvent.analyzer.entity.AnalysisMetrics;
import com.labinvent.analyzer.entity.AnalysisResult;
import com.labinvent.analyzer.entity.AnalysisResultStatus;
import com.labinvent.analyzer.repository.AnalysisResultRepository;
import com.labinvent.analyzer.service.notify.NotifyStatus;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
@AllArgsConstructor
public class AnalysisExecutor {

    private final AnalysisResultRepository repository;
    private final ProgressRegistry progressRegistry;
    private final CsvFileProcessorImpl processor;

    @Async("analysisExecutor")
    public void runAnalysis(AnalysisResult result, ProgressState state) {
        long startMillis = System.currentTimeMillis();
        Path filePath = Paths.get(result.getTempFilePath());
        try {
            long totalBytes = Files.size(filePath);

            try (ReaderBundle bundle = processor.openReader(filePath)) {
                BufferedReader br = bundle.reader();
                StatsAccumulator acc = new StatsAccumulator();
                String line;
                long lineNo = 0;
                int lastPercent = 0;

                while ((line = br.readLine()) != null) {
                    lineNo++;
                    processor.processLine(line, lineNo, acc);

                    lastPercent = updateProgressIfChanged(result.getId(), state, bundle, totalBytes, lastPercent);

                    if (state.isCancelled()) {
                        markCancelled(result);
                        return;
                    }
                }

                finalizeResult(result, acc, startMillis);
            }
        } catch (IOException e) {
            markFailed(result, startMillis, e);
        }
    }

    private int updateProgressIfChanged(Long id, ProgressState state, ReaderBundle bundle, long totalBytes, int lastPercent) {
        long readBytes = bundle.getReadBytes();
        int percent = (int) Math.min(100, (readBytes * 100.0 / totalBytes));
        if (percent > lastPercent) {
            state.setProgress(percent);
            notifyProgress(id);
            return percent;
        }
        return lastPercent;
    }

    @NotifyStatus(status = AnalysisResultStatus.DONE, withProgress = true)
    private void finalizeResult(AnalysisResult result, StatsAccumulator acc, long startMillis) {
        AnalysisMetrics metrics = AnalysisMetrics.builder()
                .count(acc.getCount())
                .minValue(acc.getMin())
                .maxValue(acc.getMax())
                .avg(acc.getMean())
                .stdDev(acc.getStdDev())
                .skipCount(acc.getInvalidCount())
                .uniqueCount(acc.getUniqueCount())
                .build();

        result.markDone(metrics, System.currentTimeMillis() - startMillis);

        repository.save(result);
        progressRegistry.remove(result.getId());
    }

    @NotifyStatus(status = AnalysisResultStatus.CANCELLED, withProgress = true)
    private void markCancelled(AnalysisResult result) {
        result.markCancelled();
        repository.save(result);
        progressRegistry.remove(result.getId());
    }

    @NotifyStatus(status = AnalysisResultStatus.FAILED, withProgress = true)
    private void markFailed(AnalysisResult result, long startMillis, Exception e) {
        log.error("Ошибка анализа id={}: {}", result.getId(), e.getMessage(), e);
        result.markFailed(System.currentTimeMillis() - startMillis);
        repository.save(result);
        progressRegistry.remove(result.getId());
    }

    @NotifyStatus(status = AnalysisResultStatus.PROCESSING, withProgress = true)
    private void notifyProgress(Long id) {
        // handled by NotificationAspect
    }
}
