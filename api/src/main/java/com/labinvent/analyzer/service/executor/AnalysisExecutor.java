package com.labinvent.analyzer.service.executor;

import com.labinvent.analyzer.entity.AnalysisResult;
import com.labinvent.analyzer.service.notify.AnalysisStatusPublisher;
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

    private final AnalysisStatusPublisher publisher;
    private final CsvFileProcessorImpl processor;

    @Async("analysisTaskExecutor")
    public void runAnalysis(AnalysisResult result, ProgressState state) {
        long startMillis = System.currentTimeMillis();
        Path filePath = Paths.get(result.getTempFilePath());

        publisher.markProcessing(result);

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
                        publisher.markCancelled(result);
                        log.info("Анализ файла id={} прерван", result.getId());
                        return;
                    }
                }

                publisher.finalizeResult(result, acc, startMillis);
                log.info("Анализ файла id={} завершён", result.getId());
            }
        } catch (IOException e) {
            publisher.markFailed(result, startMillis, e);
        }
    }

    private int updateProgressIfChanged(Long id, ProgressState state, ReaderBundle bundle, long totalBytes, int lastPercent) {
        long readBytes = bundle.getReadBytes();
        int percent = (int) Math.min(100, (readBytes * 100.0 / totalBytes));
        if (percent > lastPercent) {
            state.setProgress(percent);
            publisher.notifyProgress(id);
            return percent;
        }
        return lastPercent;
    }
}
