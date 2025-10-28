package com.labinvent.analyzer.dto;

import java.time.Instant;

public record AnalysisDetailDto(
        Long id,
        String fileName,
        long fileSizeBytes,
        String tempFilePath,
        Instant uploadedAt,
        Instant processedAt,
        long processDurationMillis,
        long count,
        double minValue,
        double maxValue,
        double avg,
        double stdDev,
        long skipCount,
        long uniqueCount,
        String status
) {}