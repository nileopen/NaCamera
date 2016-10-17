package com.na.camera;

/**
 * @actor:taotao
 * @DATE: 16/10/14
 */
public interface INaCameraEvent {
    void onCameraError(String errMsg);

    void onCameraOpening(int id);

    void onSwicthCamera(boolean isSuccess, int cameraId);

    void onCameraClosed();

    void onSwithcLight(boolean b);

    void onZoom(int mCurrentZoom, boolean isSuccess);

    void onFirstFrameAvailable();

//    void onCameraFreezed(String errorDescription);
}