package com.labinvent.analyzer.service.scheduler;

import com.labinvent.analyzer.entity.AnalysisResult;
import com.labinvent.analyzer.entity.AnalysisResultStatus;
import com.labinvent.analyzer.repository.AnalysisResultRepository;
import com.labinvent.analyzer.service.AnalysisService;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.mockito.Mockito.*;

class AnalysisSchedulerTest {

    @Test
    void testPickAndStart() {
        AnalysisService service = mock(AnalysisService.class);
        AnalysisResultRepository repo = mock(AnalysisResultRepository.class);

        AnalysisResult rec = AnalysisResult.builder().id(1L)
                .status(AnalysisResultStatus.UPLOADED).build();
        when(repo.findFirstByStatusOrderByUploadedAtAsc(AnalysisResultStatus.UPLOADED))
                .thenReturn(Optional.of(rec));

        AnalysisScheduler scheduler = new AnalysisScheduler(service, repo);
        scheduler.pickAndStart();

        verify(service).startAnalysis(1L);
    }
}
