package com.na.camera.util;

import android.hardware.Camera;

import static java.lang.Math.abs;

/**
 * Created by taotao on 16/7/15.
 */
public class SizeComparator extends ClosestComparator<Camera.Size> {
    private int mWidth;
    private int mHeight;

    public SizeComparator(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
    }

    @Override
    public int diff(Camera.Size size) {
        return abs(mWidth - size.width) + abs(mHeight - size.height);
    }
}
