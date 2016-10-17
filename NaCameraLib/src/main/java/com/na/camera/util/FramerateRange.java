package com.na.camera.util;

/**
 * Created by taotao on 16/7/15.
 */
public class FramerateRange {
    public int min;
    public int max;

    public FramerateRange(int min, int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public String toString() {
        return "[" + (min / 1000.0f) + ":" + (max / 1000.0f) + "]";
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof FramerateRange)) {
            return false;
        }
        final FramerateRange otherFramerate = (FramerateRange) other;
        return min == otherFramerate.min && max == otherFramerate.max;
    }

    @Override
    public int hashCode() {
        // Use prime close to 2^16 to avoid collisions for normal values less than 2^16.
        return 1 + 65537 * min + max;
    }
}
