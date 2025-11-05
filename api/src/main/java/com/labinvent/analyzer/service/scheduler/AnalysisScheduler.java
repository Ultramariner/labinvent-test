package com.labinvent.analyzer.service.scheduler;

import com.labinvent.analyzer.entity.AnalysisResultStatus;
import com.labinvent.analyzer.repository.AnalysisResultRepository;
import com.labinvent.analyzer.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class AnalysisScheduler {

    private final AnalysisService analysisService;
    private final AnalysisResultRepository repository;

    @Scheduled(fixedDelay = 1000)
    public void pickAndStart() {
        repository.findFirstByStatusOrderByUploadedAtAsc(AnalysisResultStatus.UPLOADED)
                .ifPresent(analysisService::startAnalysis);
    }
}
