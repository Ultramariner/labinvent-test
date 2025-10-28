package com.labinvent.analyzer.service.analysis.progress;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ProgressState {
    private final AtomicInteger progress = new AtomicInteger(0);
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    public int getProgress() {
        return progress.get();
    }

    public void setProgress(int value) {
        progress.set(value);
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    public void cancel() {
        cancelled.set(true);
    }
}