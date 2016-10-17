package com.na.camera.util;

/**
 * Created by taotao on 16/7/15.
 */
public class FramerateRangeComparator extends ClosestComparator<FramerateRange> {
    // Progressive penalty if the upper bound is further away than |MAX_FPS_DIFF_THRESHOLD|
    // from requested.
    private static final int MAX_FPS_DIFF_THRESHOLD = 5000;
    private static final int MAX_FPS_LOW_DIFF_WEIGHT = 1;
    private static final int MAX_FPS_HIGH_DIFF_WEIGHT = 3;
    // Progressive penalty if the lower bound is bigger than |MIN_FPS_THRESHOLD|.
    private static final int MIN_FPS_THRESHOLD = 8000;
    private static final int MIN_FPS_LOW_VALUE_WEIGHT = 1;
    private static final int MIN_FPS_HIGH_VALUE_WEIGHT = 4;

    private int requestedFps;

    public FramerateRangeComparator(int requestedFps) {
        this.requestedFps = requestedFps;
    }

    // Use one weight for small |value| less than |threshold|, and another weight above.
    private int progressivePenalty(int value, int threshold, int lowWeight, int highWeight) {
        return (value < threshold) ? value * lowWeight : threshold * lowWeight + (value - threshold) * highWeight;
    }

    @Override
    public int diff(FramerateRange range) {
        final int minFpsError = progressivePenalty(range.min, MIN_FPS_THRESHOLD, MIN_FPS_LOW_VALUE_WEIGHT, MIN_FPS_HIGH_VALUE_WEIGHT);
        final int maxFpsError = progressivePenalty(Math.abs(requestedFps * 1000 - range.max), MAX_FPS_DIFF_THRESHOLD, MAX_FPS_LOW_DIFF_WEIGHT, MAX_FPS_HIGH_DIFF_WEIGHT);
        return minFpsError + maxFpsError;
    }
}
