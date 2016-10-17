package com.na.camera.util;

import android.graphics.ImageFormat;

/**
 * Created by taotao on 16/7/15.
 */
public class CaptureFormat {
    public final int width;
    public final int height;
    public final FramerateRange framerate;

    // (hbos): If VideoCapturer.startCapture is updated to support other image formats then this
    // needs to be updated and VideoCapturer.getSupportedFormats need to return CaptureFormats of
    // all imageFormats.
    public final int imageFormat = ImageFormat.NV21;

    public CaptureFormat(int width, int height, int minFramerate, int maxFramerate) {
        this.width = width;
        this.height = height;
        this.framerate = new FramerateRange(minFramerate, maxFramerate);
    }

    public CaptureFormat(int width, int height, FramerateRange framerate) {
        this.width = width;
        this.height = height;
        this.framerate = framerate;
    }

    // Calculates the frame size of the specified image format. Currently only
    // supporting ImageFormat.NV21.
    // The size is width * height * number of bytes per pixel.
    // http://developer.android.com/reference/android/hardware/Camera.html#addCallbackBuffer(byte[])
    public static int frameSize(int width, int height, int imageFormat) {
        if (imageFormat != ImageFormat.NV21) {
            throw new UnsupportedOperationException("Don't know how to calculate "
                    + "the frame size of non-NV21 image formats.");
        }
        return (width * height * ImageFormat.getBitsPerPixel(imageFormat)) / 8;
    }

    // Calculates the frame size of this capture format.
    public int frameSize() {
        return frameSize(width, height, imageFormat);
    }

    @Override
    public String toString() {
        return width + "x" + height + "@" + framerate;
    }

    public boolean isSameFormat(final CaptureFormat that) {
        return that != null
                && width == that.width
                && height == that.height
                && framerate.equals(that.framerate);
    }
}
