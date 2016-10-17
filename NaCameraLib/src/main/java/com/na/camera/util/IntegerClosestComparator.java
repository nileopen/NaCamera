package com.na.camera.util;

import static java.lang.Math.abs;

/**
 * Created by taotao on 16/7/15.
 */
public class IntegerClosestComparator extends ClosestComparator<Integer> {
    private int mFrameRate;

    public IntegerClosestComparator(int framerate) {
        this.mFrameRate = framerate;
    }

    @Override
    public int diff(Integer value) {
        return abs(value - mFrameRate);
    }
}
