package com.labinvent.analyzer.service.analysis.scheduler;

import com.labinvent.analyzer.entity.AnalysisRecordStatus;
import com.labinvent.analyzer.repository.AnalysisRecordRepository;
import com.labinvent.analyzer.service.analysis.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class AnalysisScheduler {

    private final AnalysisService analysisService;
    private final AnalysisRecordRepository repository;

    @Scheduled(fixedDelay = 5000)
    public void pickAndStart() {
        repository.findFirstByStatusOrderByUploadedAtAsc(AnalysisRecordStatus.UPLOADED)
                .ifPresent(record -> analysisService.startAnalysis(record.getId()));
    }
}
