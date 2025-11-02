package com.labinvent.analyzer.service.analysis.scheduler;

import com.labinvent.analyzer.entity.AnalysisRecord;
import com.labinvent.analyzer.entity.AnalysisRecordStatus;
import com.labinvent.analyzer.repository.AnalysisRecordRepository;
import com.labinvent.analyzer.service.analysis.AnalysisService;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.mockito.Mockito.*;

class AnalysisSchedulerTest {

    @Test
    void testPickAndStart() {
        AnalysisService service = mock(AnalysisService.class);
        AnalysisRecordRepository repo = mock(AnalysisRecordRepository.class);

        AnalysisRecord rec = AnalysisRecord.builder().id(1L)
                .status(AnalysisRecordStatus.UPLOADED).build();
        when(repo.findFirstByStatusOrderByUploadedAtAsc(AnalysisRecordStatus.UPLOADED))
                .thenReturn(Optional.of(rec));

        AnalysisScheduler scheduler = new AnalysisScheduler(service, repo);
        scheduler.pickAndStart();

        verify(service).startAnalysis(1L);
    }
}
