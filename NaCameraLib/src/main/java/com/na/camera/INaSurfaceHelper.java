package com.na.camera;

import android.graphics.SurfaceTexture;
import android.view.SurfaceHolder;

/**
 * @actor:taotao
 * @DATE: 16/10/14
 */
public interface INaSurfaceHelper {
    SurfaceTexture getSurfaceTexture();

    SurfaceHolder getSurfaceHolder();

    void onPreviewFrame(byte[] data, int width, int height, int orientation, long captureTimeNs);

    void onRotation(int orientation, boolean isBack);

//    void onPreviewFrame(byte[] data, Camera camera);
}

