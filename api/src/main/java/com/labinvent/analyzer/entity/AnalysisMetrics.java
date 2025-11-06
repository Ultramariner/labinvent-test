package com.labinvent.analyzer.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisMetrics {
    private long count;
    private double minValue;
    private double maxValue;
    private double avg;
    private double stdDev;
    private long skipCount;
    private long uniqueCount;
}