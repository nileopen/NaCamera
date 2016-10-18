package cn.uplus.filter.camera;

/**
 * Created by taotao on 16/7/8.
 */
public interface CameraEventHandler {
    void onError(String error);
    void onOpening(int cameraId);
}
