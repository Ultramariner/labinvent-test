package com.labinvent.analyzer.service.executor;

import com.labinvent.analyzer.entity.AnalysisResult;
import com.labinvent.analyzer.entity.AnalysisResultStatus;
import com.labinvent.analyzer.repository.AnalysisResultRepository;
import com.labinvent.analyzer.service.notify.AnalysisStatusPublisher;
import com.labinvent.analyzer.service.progress.ProgressRegistry;
import com.labinvent.analyzer.service.progress.ProgressState;
import com.labinvent.analyzer.util.impl.CsvFileProcessorImpl;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AnalysisExecutorTest {

    @Test
    void testRunAnalysisSimpleCsv() throws Exception {
        Path tmp = Files.createTempFile("test", ".csv");
        Files.writeString(tmp, "header\n1,2.0\n2,4.0\n");

        AnalysisResult result = AnalysisResult.builder()
                .id(1L)
                .status(AnalysisResultStatus.UPLOADED)
                .tempFilePath(tmp.toString())
                .build();

        AnalysisResultRepository repo = mock(AnalysisResultRepository.class);
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(repo.findById(1L)).thenReturn(Optional.of(result));

        ProgressRegistry registry = new ProgressRegistry();
        CsvFileProcessorImpl processor = new CsvFileProcessorImpl();

        AnalysisStatusPublisher publisher = new AnalysisStatusPublisher(repo, registry);
        AnalysisExecutor executor = new AnalysisExecutor(publisher, processor);

        ProgressState state = registry.getOrCreate(1L);
        executor.runAnalysis(result, state);

        assertEquals(AnalysisResultStatus.DONE, result.getStatus());
        assertNotNull(result.getMetrics());
        assertEquals(2, result.getMetrics().getCount());
        assertEquals(3.0, result.getMetrics().getAvg(), 0.001);

        assertNull(registry.get(1L));

        verify(repo, atLeastOnce()).save(result);
    }
}
