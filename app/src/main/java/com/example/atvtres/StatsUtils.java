package com.example.atvtres;

import java.util.Arrays;

public class StatsUtils {

    public static double calculateMean(double[] data) {
        return Arrays.stream(data).average().orElse(Double.NaN);
    }

    public static double calculateStandardDeviation(double[] data) {
        double mean = calculateMean(data);
        return Math.sqrt(Arrays.stream(data)
                .map(x -> Math.pow(x - mean, 2))
                .average().orElse(Double.NaN));
    }

    public static double calculateBias(double[] data, double[] trueValues) {
        if (data.length != trueValues.length) return Double.NaN;
        return calculateMean(Arrays.stream(data)
                .map(i -> i - trueValues[(int) i])
                .toArray());
    }

    public static double calculatePrecision(double[] data) {
        return calculateStandardDeviation(data); // Assuming precision is std dev
    }

    public static double calculateUncertainty(double[] data) {
        return calculateStandardDeviation(data) / Math.sqrt(data.length); // Example for uncertainty
    }
}
