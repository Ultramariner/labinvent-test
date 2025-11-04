package com.labinvent.analyzer.util;

import java.util.HashSet;

public final class StatsAccumulator {
    private long count;
    private double mean;
    private double m2;
    private double min = Double.POSITIVE_INFINITY;
    private double max = Double.NEGATIVE_INFINITY;
    private long invalidCount;
    private final HashSet<Double> uniques = new HashSet<>();

    public void addValid(double value) {
        count++;
        double delta = value - mean;
        mean += delta / count;
        double delta2 = value - mean;
        m2 += delta * delta2;

        if (value < min) min = value;
        if (value > max) max = value;
        uniques.add(value);
    }

    public void addInvalid() {
        invalidCount++;
    }

    public long getCount() {
        return count;
    }

    public double getMean() {
        return count > 0 ? mean : Double.NaN;
    }

    public double getStdDev() {
        if (count <= 1) return Double.NaN;
        double variance = m2 / (count - 1);
        return Math.sqrt(variance);
    }

    public double getMin() {
        return count > 0 ? min : Double.NaN;
    }

    public double getMax() {
        return count > 0 ? max : Double.NaN;
    }

    public long getInvalidCount() {
        return invalidCount;
    }

    public long getUniqueCount() {
        return uniques.size();
    }
}
