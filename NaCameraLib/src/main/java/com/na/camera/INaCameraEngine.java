package com.na.camera;

/**
 * @actor:taotao
 * @DATE: 16/10/14
 */
public interface INaCameraEngine {
    int DEFAULT_WIDTH = 1280;
    int DEFAULT_HEIGHT = 720;
    int DEFAULT_FRAME_RATE = 24;

    void setCameraEvent(INaCameraEvent event);

    void openCamera(int cameraId);

    void switchCamera();

    void closeCamera();

    void switchLight(boolean isOpen);

    void destroy();

    void setSurfaceHelper(INaSurfaceHelper helper);

    void setZoom(float scale);

    void setFocus();
}
