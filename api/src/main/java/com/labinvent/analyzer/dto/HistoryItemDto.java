package com.labinvent.analyzer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record HistoryItemDto(
        Long id,
        String fileName,
        Long fileSizeBytes,
        Long processDurationMillis,
        Double avg,
        Double stdDev,
        String status
) {}