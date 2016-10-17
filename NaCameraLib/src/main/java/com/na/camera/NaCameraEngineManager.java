package com.na.camera;

/**
 * @actor:taotao
 * @DATE: 16/10/14
 */
public class NaCameraEngineManager implements INaCameraEngine, INaCameraEvent{
    private final static String Tag = "NaCameraManager";
    private static int sThreadId = 1000;
    private static NaCameraEngineManager sInstance;

//    private HandlerThread mCameraThread;
//    private Handler mCameraHanlder;

    private INaCameraEvent mCameraEvent;
    private INaCameraEngine mCameraEngine;

    public static NaCameraEngineManager getInstance() {
        if (sInstance == null) {
            synchronized (NaCameraEngineManager.class) {
                if (sInstance == null) {
                    sInstance = new NaCameraEngineManager();
                    sInstance.init();
                }
            }
        }
        return sInstance;
    }

//    private int getThreadId() {
//        return sThreadId++;
//    }

    private void init() {
//        if (mCameraThread != null) {
//            mCameraThread = new HandlerThread(Tag + "-" + getThreadId());
//            mCameraThread.start();
//            mCameraHanlder = new Handler(mCameraThread.getLooper());
//        }

        if (mCameraEngine == null) {
            mCameraEngine = new NaCameraEngine1();
        }
    }

    private void release() {
//        if (mCameraThread != null) {
//            mCameraThread.quit();
//        }
//        mCameraThread = null;
//        mCameraHanlder = null;
        sInstance = null;
    }

    @Override
    public void setCameraEvent(INaCameraEvent event) {
        this.mCameraEvent = event;
        if (mCameraEngine != null) {
            mCameraEngine.setCameraEvent(this);
        }
    }

    @Override
    public void openCamera(int cameraId) {
        if (mCameraEngine != null) {
            mCameraEngine.openCamera(cameraId);
        }
    }

    @Override
    public void switchCamera() {
        if (mCameraEngine != null) {
            mCameraEngine.switchCamera();
        }
    }

    @Override
    public void closeCamera() {
        if (mCameraEngine != null) {
            mCameraEngine.closeCamera();
        }
    }

    @Override
    public void switchLight(boolean isOpen) {
        if (mCameraEngine != null) {
            mCameraEngine.switchLight(isOpen);
        }
    }

    @Override
    public void destroy() {
        if (mCameraEngine != null) {
            mCameraEngine.destroy();
        }
        mCameraEngine = null;
        mCameraEvent = null;
        release();
    }

    @Override
    public void setSurfaceHelper(INaSurfaceHelper helper) {
        if (mCameraEngine != null) {
            mCameraEngine.setSurfaceHelper(helper);
        }
    }

    @Override
    public void setZoom(float scale) {
        if (mCameraEngine != null) {
            mCameraEngine.setZoom(scale);
        }
    }

    @Override
    public void setFocus() {
        if (mCameraEngine != null) {
            mCameraEngine.setFocus();
        }
    }

    @Override
    public void onCameraError(String errorDescription) {
        if (mCameraEvent != null) {
            mCameraEvent.onCameraError(errorDescription);
        }
    }

//    @Override
//    public void onCameraFreezed(String errorDescription) {
//        if (mCameraEvent != null) {
//            mCameraEvent.onCameraFreezed(errorDescription);
//        }
//    }

    @Override
    public void onCameraOpening(int cameraId) {
        if (mCameraEvent != null) {
            mCameraEvent.onCameraOpening(cameraId);
        }
    }

    @Override
    public void onFirstFrameAvailable() {
        if (mCameraEvent != null) {
            mCameraEvent.onFirstFrameAvailable();
        }
    }

    @Override
    public void onCameraClosed() {
        if (mCameraEvent != null) {
            mCameraEvent.onCameraClosed();
        }
    }

    @Override
    public void onSwicthCamera(boolean isSuccess, int cameraId) {
        if (mCameraEvent != null) {
            mCameraEvent.onSwicthCamera(isSuccess, cameraId);
        }
    }

    @Override
    public void onSwithcLight(boolean isSuccess) {
        if (mCameraEvent != null) {
            mCameraEvent.onSwithcLight(isSuccess);
        }
    }

    @Override
    public void onZoom(int curZoom, boolean isSuccess) {
        if (mCameraEvent != null) {
            mCameraEvent.onZoom(curZoom, isSuccess);
        }
    }
}
