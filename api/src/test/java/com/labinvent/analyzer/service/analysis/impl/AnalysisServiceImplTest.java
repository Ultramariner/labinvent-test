package com.labinvent.analyzer.service.analysis.impl;

import com.labinvent.analyzer.entity.AnalysisRecord;
import com.labinvent.analyzer.entity.AnalysisRecordStatus;
import com.labinvent.analyzer.mapper.AnalysisMapper;
import com.labinvent.analyzer.repository.AnalysisRecordRepository;
import com.labinvent.analyzer.service.analysis.notify.AnalysisNotifier;
import com.labinvent.analyzer.service.analysis.progress.ProgressRegistry;
import com.labinvent.analyzer.service.analysis.progress.ProgressState;
import com.labinvent.analyzer.service.storage.StorageService;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AnalysisServiceImplTest {

    @Test
    void testRegisterFile() {
        AnalysisRecordRepository repo = mock(AnalysisRecordRepository.class);
        AnalysisRecord saved = AnalysisRecord.builder().id(1L).build();
        when(repo.save(any())).thenReturn(saved);

        AnalysisServiceImpl service = new AnalysisServiceImpl(repo,
                mock(StorageService.class),
                new ProgressRegistry(),
                mock(AnalysisMapper.class),
                mock(AnalysisNotifier.class));

        Long id = service.registerFile("file.csv", 100, "/tmp/file.csv");
        assertEquals(1L, id);
        verify(repo).save(any());
    }

    @Test
    void testRunAnalysisSimpleCsv() throws Exception {
        Path tmp = Files.createTempFile("test", ".csv");
        Files.writeString(tmp, "header\n1,2.0\n2,4.0\n");

        AnalysisRecord record = AnalysisRecord.builder()
                .id(1L).status(AnalysisRecordStatus.UPLOADED)
                .tempFilePath(tmp.toString()).build();

        AnalysisRecordRepository repo = mock(AnalysisRecordRepository.class);
        when(repo.findById(1L)).thenReturn(Optional.of(record));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AnalysisNotifier notifier = mock(AnalysisNotifier.class);

        AnalysisServiceImpl service = new AnalysisServiceImpl(repo,
                mock(StorageService.class),
                new ProgressRegistry(),
                mock(AnalysisMapper.class),
                notifier);

        service.runAnalysis(1L, new ProgressState(), tmp.toString());

        assertEquals(AnalysisRecordStatus.DONE, record.getStatus());
        assertEquals(2, record.getCount());
        assertEquals(3.0, record.getAvg());
        verify(notifier, atLeastOnce()).notifyStatus(eq(1L), anyString(), anyInt());
    }
}
