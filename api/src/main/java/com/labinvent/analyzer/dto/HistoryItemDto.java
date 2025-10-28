package com.labinvent.analyzer.dto;

public record HistoryItemDto(
        Long id,
        String fileName,
        long fileSizeBytes,
        long processDurationMillis,
        double avg,
        double stdDev,
        String status
) {}