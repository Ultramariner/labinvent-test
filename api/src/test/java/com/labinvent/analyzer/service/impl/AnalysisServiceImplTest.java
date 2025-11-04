package com.labinvent.analyzer.service.impl;

import com.labinvent.analyzer.entity.AnalysisResult;
import com.labinvent.analyzer.entity.AnalysisResultStatus;
import com.labinvent.analyzer.mapper.AnalysisMapper;
import com.labinvent.analyzer.repository.AnalysisResultRepository;
import com.labinvent.analyzer.service.notify.AnalysisNotifier;
import com.labinvent.analyzer.service.progress.ProgressRegistry;
import com.labinvent.analyzer.service.progress.ProgressState;
import com.labinvent.analyzer.service.StorageService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

//todo переписать под новую структуру и логику
class AnalysisServiceImplTest {

    @Test
    void testRegisterFile() {
        AnalysisResultRepository repo = mock(AnalysisResultRepository.class);
        AnalysisResult saved = AnalysisResult.builder().id(1L).build();
        when(repo.save(any())).thenReturn(saved);

        AnalysisServiceImpl service = new AnalysisServiceImpl(repo,
                mock(StorageService.class),
                new ProgressRegistry(),
                mock(AnalysisMapper.class),
                Mockito.mock(AnalysisNotifier.class));

        service.registerFile("file.csv", 100, "/tmp/file.csv");
        verify(repo).save(any());
    }

    @Test
    void testRunAnalysisSimpleCsv() throws Exception {
        Path tmp = Files.createTempFile("test", ".csv");
        Files.writeString(tmp, "header\n1,2.0\n2,4.0\n");

        AnalysisResult record = AnalysisResult.builder()
                .id(1L).status(AnalysisResultStatus.UPLOADED)
                .tempFilePath(tmp.toString()).build();

        AnalysisResultRepository repo = mock(AnalysisResultRepository.class);
        when(repo.findById(1L)).thenReturn(Optional.of(record));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AnalysisNotifier notifier = mock(AnalysisNotifier.class);

        AnalysisServiceImpl service = new AnalysisServiceImpl(repo,
                mock(StorageService.class),
                new ProgressRegistry(),
                mock(AnalysisMapper.class),
                notifier);

        service.runAnalysis(1L, new ProgressState(), tmp.toString());

        assertEquals(AnalysisResultStatus.DONE, record.getStatus());
        assertEquals(2, record.getCount());
        assertEquals(3.0, record.getAvg());
        verify(notifier, atLeastOnce()).notifyStatus(eq(1L), anyString(), anyInt());
    }
}
