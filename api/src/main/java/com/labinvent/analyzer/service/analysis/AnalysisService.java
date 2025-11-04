package com.labinvent.analyzer.service.analysis;

import com.labinvent.analyzer.dto.AnalysisDetailDto;
import com.labinvent.analyzer.dto.HistoryItemDto;

import java.util.List;

public interface AnalysisService {
    Long registerFile(String fileName, long size, String path);

    void startAnalysis(Long id);

    void cancel(Long id);

    int getProgress(Long id);

    List<HistoryItemDto> getHistory();

    AnalysisDetailDto getDetail(Long id);

    void delete(Long id);
}