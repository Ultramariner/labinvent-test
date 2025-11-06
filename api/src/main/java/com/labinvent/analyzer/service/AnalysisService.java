package com.labinvent.analyzer.service;

import com.labinvent.analyzer.dto.AnalysisDetailDto;
import com.labinvent.analyzer.dto.HistoryItemDto;
import com.labinvent.analyzer.entity.AnalysisResult;

import java.util.List;

public interface AnalysisService {
    void registerFile(String fileName, long size, String path);

    void startAnalysis(Long id);

    void startAnalysis(AnalysisResult result);

    void cancel(Long id);

    int getProgress(Long id);

    List<HistoryItemDto> getHistory();

    AnalysisDetailDto getDetail(Long id);

    void delete(Long id);
}