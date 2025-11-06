package com.labinvent.analyzer.mapper;

import com.labinvent.analyzer.dto.*;
import com.labinvent.analyzer.entity.AnalysisResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AnalysisMapper {

    @Mapping(target = "processDurationMillis", source = "processDurationMillis")
    @Mapping(target = "avg", source = "metrics.avg")
    @Mapping(target = "stdDev", source = "metrics.stdDev")
    HistoryItemDto toHistoryItem(AnalysisResult entity);

    @Mapping(target = "count", source = "metrics.count")
    @Mapping(target = "minValue", source = "metrics.minValue")
    @Mapping(target = "maxValue", source = "metrics.maxValue")
    @Mapping(target = "avg", source = "metrics.avg")
    @Mapping(target = "stdDev", source = "metrics.stdDev")
    @Mapping(target = "skipCount", source = "metrics.skipCount")
    @Mapping(target = "uniqueCount", source = "metrics.uniqueCount")
    AnalysisDetailDto toDetail(AnalysisResult entity);
}