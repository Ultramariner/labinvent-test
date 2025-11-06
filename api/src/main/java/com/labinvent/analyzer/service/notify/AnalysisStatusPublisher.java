package com.labinvent.analyzer.service.notify;

import com.labinvent.analyzer.entity.AnalysisMetrics;
import com.labinvent.analyzer.entity.AnalysisResult;
import com.labinvent.analyzer.entity.AnalysisResultStatus;
import com.labinvent.analyzer.repository.AnalysisResultRepository;
import com.labinvent.analyzer.service.progress.ProgressRegistry;
import com.labinvent.analyzer.util.StatsAccumulator;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class AnalysisStatusPublisher {

    private final AnalysisResultRepository repository;
    private final ProgressRegistry progressRegistry;

    @Transactional
    @NotifyStatus(status = AnalysisResultStatus.PROCESSING, withProgress = true)
    public void markProcessing(AnalysisResult result) {
        result.markProcessing();
        repository.save(result);
    }

    @Transactional
    @NotifyStatus(status = AnalysisResultStatus.DONE, withProgress = true)
    public void finalizeResult(AnalysisResult result, StatsAccumulator acc, long startMillis) {
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

    @Transactional
    @NotifyStatus(status = AnalysisResultStatus.CANCELLED, withProgress = true)
    public void markCancelled(AnalysisResult result) {
        result.markCancelled();
        repository.save(result);
        progressRegistry.remove(result.getId());
    }

    @Transactional
    @NotifyStatus(status = AnalysisResultStatus.FAILED, withProgress = true)
    public void markFailed(AnalysisResult result, long duration, Exception e) {
        log.error("Ошибка анализа id={}: {}", result.getId(), e.getMessage(), e);
        result.markFailed(duration);
        repository.save(result);
        progressRegistry.remove(result.getId());
    }

    @NotifyStatus(status = AnalysisResultStatus.PROCESSING, withProgress = true)
    public void notifyProgress(Long id) {
        // handled by NotificationAspect
    }
}
