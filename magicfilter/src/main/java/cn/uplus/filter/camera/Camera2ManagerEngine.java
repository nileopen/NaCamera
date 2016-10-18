package cn.uplus.filter.camera;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by taotao on 16/7/8.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class Camera2ManagerEngine {
    private final static String Tag = "Camera2ManagerEngine";
    private CameraEventHandler eventHandler;
    private CameraDevice mCamera;

    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            mCamera = camera;
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            if (camera != null) {
                camera.close();
            }
            mCamera = null;
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            if (camera != null) {
                camera.close();
            }
            mCamera = null;
            if (eventHandler != null) {
                eventHandler.onError("camera error:" + error);
            }
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d(Tag, "handleMessage what=" + msg.what);
        }
    };

    public void open(Context context, String cameraId, int with, int height) {
        try {
            CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            cameraManager.openCamera(cameraId, stateCallback, handler);
        } catch (Exception e) {
            if (eventHandler != null) {
                eventHandler.onError(e.getMessage());
            }
        }
    }

}
