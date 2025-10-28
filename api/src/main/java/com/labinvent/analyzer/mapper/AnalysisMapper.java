package com.labinvent.analyzer.mapper;

import com.labinvent.analyzer.dto.*;
import com.labinvent.analyzer.entity.AnalysisRecord;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AnalysisMapper {

    HistoryItemDto toHistoryItem(AnalysisRecord entity);

    AnalysisDetailDto toDetail(AnalysisRecord entity);
}