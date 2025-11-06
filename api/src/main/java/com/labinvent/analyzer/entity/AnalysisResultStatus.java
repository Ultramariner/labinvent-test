package com.labinvent.analyzer.entity;

public enum AnalysisResultStatus {
    UPLOADED,
    PROCESSING,
    DONE,
    CANCELLED,
    FAILED;

    public boolean canStart() {
        return this == UPLOADED || this == CANCELLED;
    }
}
