package com.labinvent.analyzer.service.impl;

import com.labinvent.analyzer.entity.AnalysisResult;
import com.labinvent.analyzer.entity.AnalysisResultStatus;
import com.labinvent.analyzer.mapper.AnalysisMapper;
import com.labinvent.analyzer.repository.AnalysisResultRepository;
import com.labinvent.analyzer.service.executor.AnalysisExecutor;
import com.labinvent.analyzer.service.progress.ProgressRegistry;
import com.labinvent.analyzer.service.progress.ProgressState;
import com.labinvent.analyzer.service.StorageService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AnalysisServiceImplTest {

    @Test
    void testRegisterFile() {
        AnalysisResultRepository repo = mock(AnalysisResultRepository.class);
        StorageService storage = mock(StorageService.class);
        AnalysisMapper mapper = mock(AnalysisMapper.class);
        AnalysisExecutor executor = mock(AnalysisExecutor.class);

        AnalysisResult saved = AnalysisResult.builder().id(1L).build();
        when(repo.save(any())).thenReturn(saved);

        AnalysisServiceImpl service = new AnalysisServiceImpl(
                repo, storage, new ProgressRegistry(), mapper, executor
        );

        service.registerFile("file.csv", 100, "/tmp/file.csv");

        verify(repo).save(any());
    }

    @Test
    void testStartAnalysisDelegatesToExecutor() {
        AnalysisResultRepository repo = mock(AnalysisResultRepository.class);
        StorageService storage = mock(StorageService.class);
        AnalysisMapper mapper = mock(AnalysisMapper.class);
        AnalysisExecutor executor = mock(AnalysisExecutor.class);

        ProgressRegistry registry = new ProgressRegistry();

        AnalysisResult result = AnalysisResult.builder()
                .id(1L)
                .status(AnalysisResultStatus.UPLOADED)
                .tempFilePath("/tmp/file.csv")
                .build();

        when(repo.findById(1L)).thenReturn(Optional.of(result));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AnalysisServiceImpl service = new AnalysisServiceImpl(repo, storage, registry, mapper, executor);

        service.startAnalysis(1L);

        verify(executor).runAnalysis(eq(result), any(ProgressState.class));
    }
}
