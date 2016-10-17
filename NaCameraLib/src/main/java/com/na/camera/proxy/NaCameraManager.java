package com.na.camera.proxy;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.SurfaceHolder;

import com.na.uitls.NaLog;

import java.io.IOException;

/**
 * @actor:taotao
 * @DATE: 16/10/14
 */
public class NaCameraManager {
    private static final String TAG = "CameraManager";

    private static final int CAMERA_MSG_RELEASE = 1;
    private static final int CAMERA_MSG_RECONNECT = 2;
    private static final int CAMERA_MSG_UNLOCK = 3;
    private static final int CAMERA_MSG_LOCK = 4;
    private static final int CAMERA_MSG_SET_PREVIEW_TEXTURE_ASYNC = 5;
    private static final int CAMERA_MSG_START_PREVIEW_ASYNC = 6;
    private static final int CAMERA_MSG_STOP_PREVIEW = 7;
    private static final int CAMERA_MSG_SET_PREVIEW_VB_WITHBUFFER = 8;
    private static final int CAMERA_MSG_ADD_CB_BUFFER = 9;
    private static final int CAMERA_MSG_AUTOFOUCS = 10;
    private static final int CAMERA_MSG_AUTOFOUCS_CANCEL = 11;
    private static final int CAMERA_MSG_AUTOFOUCS_MOVE_CB = 12;
    private static final int CAMERA_MSG_SET_DISPLAY_ORIENT = 13;
    private static final int CAMERA_MSG_SET_ZOOM_CHANGE_LISTENER = 14;
    private static final int CAMERA_MSG_SET_FACE_DETECTION_LISTENER = 15;
    private static final int CAMERA_MSG_START_FACE_DETECTION = 16;
    private static final int CAMERA_MSG_STOP_FACE_DETECTION = 17;
    private static final int CAMERA_MSG_SET_ERROR_CB = 18;
    private static final int CAMERA_MSG_SET_PARAMS = 19;
    private static final int CAMERA_MSG_GET_PARAMS = 20;
    private static final int CAMERA_MSG_SET_PARAMS_ASYNC = 21;
    private static final int CAMERA_MSG_SET_ONESHOT_PREVIEW_CB = 22;
    private static final int CAMERA_MSG_SET_PREVIEW_TEXTURE = 23;
    private static final int CAMERA_MSG_SET_PREVIEW_DISPLAY = 24;
    private static final int CAMERA_MSG_START_PREVIEW = 25;
    private static final int CAMERA_MSG_SET_PREVIEW_VB = 26;


    private static NaCameraManager sCameraManager = new NaCameraManager();
    private Camera mCamera;
    private Handler mCameraHandler;
    private CameraProxy mCameraProxy;
    private Camera.Parameters mParameters;
    private IOException mReconnectException;
    private ConditionVariable mSig = new ConditionVariable();
    private CameraExceptionListener mExceptionListener;

    private NaCameraManager() {
        HandlerThread localHandlerThread = new HandlerThread("Camera Handler Thread");
        localHandlerThread.start();
        this.mCameraHandler = new CameraHandler(localHandlerThread.getLooper());
    }

    public static NaCameraManager instance() {
        return sCameraManager;
    }

    public static int getNumberOfCameras() {
        return Camera.getNumberOfCameras();
    }

    public CameraProxy cameraOpen(int cameraId) {
        CameraProxy localCameraProxy = null;
        this.mCamera = Camera.open(cameraId);
        if (this.mCamera != null) {
            this.mCameraProxy = new CameraProxy();
            localCameraProxy = this.mCameraProxy;
        }
        return localCameraProxy;
    }

    public interface CameraExceptionListener {
        void onException(Throwable e);
    }

    private class CameraHandler extends Handler {

        public CameraHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message message) {
            try {
                switch (message.what) {
                    case CAMERA_MSG_RELEASE: {
                        mCamera.release();
                        mCamera = null;
                        mCameraProxy = null;
                        break;
                    }
                    case CAMERA_MSG_RECONNECT: {
                        mReconnectException = null;
                        try {
                            mCamera.reconnect();
                        } catch (IOException e) {
                            NaLog.e(TAG, "Camera reconnect error!", e);
                            mReconnectException = e;
                        }
                        break;
                    }
                    case CAMERA_MSG_UNLOCK: {
                        mCamera.unlock();
                        break;
                    }
                    case CAMERA_MSG_LOCK: {
                        mCamera.lock();
                        break;
                    }
                    case CAMERA_MSG_SET_PREVIEW_TEXTURE_ASYNC: {
                        mCamera.setPreviewTexture((SurfaceTexture) message.obj);
                        break;
                    }
                    case CAMERA_MSG_START_PREVIEW_ASYNC: {
                        mCamera.startPreview();
                        return;
                    }
                    case CAMERA_MSG_STOP_PREVIEW: {
                        mCamera.stopPreview();
                        break;
                    }
                    case CAMERA_MSG_SET_PREVIEW_VB_WITHBUFFER: {
                        mCamera.setPreviewCallbackWithBuffer((Camera.PreviewCallback) message.obj);
                        break;
                    }
                    case CAMERA_MSG_ADD_CB_BUFFER: {
                        mCamera.addCallbackBuffer((byte[]) message.obj);
                        break;
                    }
                    case CAMERA_MSG_AUTOFOUCS: {
                        mCamera.autoFocus((Camera.AutoFocusCallback) message.obj);
                        break;
                    }
                    case CAMERA_MSG_AUTOFOUCS_CANCEL: {
                        mCamera.cancelAutoFocus();
                        break;
                    }
                    case CAMERA_MSG_AUTOFOUCS_MOVE_CB: {
                        mCamera.setAutoFocusMoveCallback((Camera.AutoFocusMoveCallback) message.obj);
                        break;
                    }
                    case CAMERA_MSG_SET_DISPLAY_ORIENT: {
                        mCamera.setDisplayOrientation(message.arg1);
                        break;
                    }
                    case CAMERA_MSG_SET_ZOOM_CHANGE_LISTENER: {
                        mCamera.setZoomChangeListener((Camera.OnZoomChangeListener) message.obj);
                        break;
                    }
                    case CAMERA_MSG_SET_FACE_DETECTION_LISTENER: {
                        mCamera.setFaceDetectionListener((Camera.FaceDetectionListener) message.obj);
                        break;
                    }
                    case CAMERA_MSG_START_FACE_DETECTION: {
                        mCamera.startFaceDetection();
                        break;
                    }
                    case CAMERA_MSG_STOP_FACE_DETECTION: {
                        mCamera.stopFaceDetection();
                        break;
                    }
                    case CAMERA_MSG_SET_ERROR_CB: {
                        mCamera.setErrorCallback((Camera.ErrorCallback) message.obj);
                        break;
                    }
                    case CAMERA_MSG_SET_PARAMS: {
                        mCamera.setParameters((Camera.Parameters) message.obj);
                        break;
                    }
                    case CAMERA_MSG_GET_PARAMS: {
                        mParameters = mCamera.getParameters();
                        break;
                    }
                    case CAMERA_MSG_SET_PARAMS_ASYNC: {
                        mCamera.setParameters((Camera.Parameters) message.obj);
                        return;
                    }
                    case CAMERA_MSG_SET_ONESHOT_PREVIEW_CB: {
                        mCamera.setOneShotPreviewCallback((Camera.PreviewCallback) message.obj);
                        break;
                    }
                    case CAMERA_MSG_SET_PREVIEW_TEXTURE: {
                        mCamera.setPreviewTexture((SurfaceTexture) message.obj);
                        break;
                    }
                    case CAMERA_MSG_SET_PREVIEW_DISPLAY: {
                        mCamera.setPreviewDisplay((SurfaceHolder) message.obj);
                        break;
                    }
                    case CAMERA_MSG_START_PREVIEW: {
                        mCamera.startPreview();
                        break;
                    }
                    case CAMERA_MSG_SET_PREVIEW_VB:{
                        mCamera.setPreviewCallback((Camera.PreviewCallback) message.obj);
                        break;
                    }
                    default: {
                        break;
                    }
                }
            } catch (Throwable e) {
                NaLog.e(TAG, "Exception camera error", e);
                if (message.what != CAMERA_MSG_RELEASE && mCamera != null) {
                    try {
                        mCamera.release();
                    } catch (Throwable e1) {
                        NaLog.e(TAG, "Exception camera release error", e1);
                    }
                    mCamera = null;
                    mCameraProxy = null;
                }

                if (mExceptionListener != null) {
                    mExceptionListener.onException(e);
                }
            }
            mSig.open();
        }
    }

    public class CameraProxy {
        private CameraProxy() {
            NaCameraUtil.Assert(mCamera != null);
        }

        public Camera getCamera() {
            return mCamera;
        }

        public void autoFocus(Camera.AutoFocusCallback paramAutoFocusCallback) {
            mSig.close();
            mCameraHandler.obtainMessage(CAMERA_MSG_AUTOFOUCS, paramAutoFocusCallback).sendToTarget();
            mSig.block();
        }

        public void cancelAutoFocus() {
            mSig.close();
            mCameraHandler.sendEmptyMessage(CAMERA_MSG_AUTOFOUCS_CANCEL);
            mSig.block();
        }

        public Camera.Parameters getParameters() {
            mSig.close();
            mCameraHandler.sendEmptyMessage(CAMERA_MSG_GET_PARAMS);
            mSig.block();
            return mParameters;
        }

        public void release() {
            mSig.close();
            mCameraHandler.sendEmptyMessage(CAMERA_MSG_RELEASE);
            mSig.block();
        }

        public void lock() {
            mSig.close();
            mCameraHandler.sendEmptyMessage(CAMERA_MSG_LOCK);
            mSig.block();
        }

        public void reconnect() throws IOException {
            mSig.close();
            mCameraHandler.sendEmptyMessage(CAMERA_MSG_RECONNECT);
            mSig.block();
            if (mReconnectException != null) {
                throw mReconnectException;
            }
        }

        public void removeAllAsyncMessage() {
            mCameraHandler.removeMessages(CAMERA_MSG_SET_PREVIEW_TEXTURE_ASYNC);
            mCameraHandler.removeMessages(CAMERA_MSG_START_PREVIEW_ASYNC);
            mCameraHandler.removeMessages(CAMERA_MSG_SET_PARAMS_ASYNC);
        }

        public void setAutoFocusMoveCallback(Camera.AutoFocusMoveCallback paramAutoFocusMoveCallback) {
            mSig.close();
            mCameraHandler.obtainMessage(CAMERA_MSG_AUTOFOUCS_MOVE_CB, paramAutoFocusMoveCallback).sendToTarget();
            mSig.block();
        }

        public void setDisplayOrientation(int paramInt) {
            mSig.close();
            mCameraHandler.obtainMessage(CAMERA_MSG_SET_DISPLAY_ORIENT, paramInt, 0).sendToTarget();
            mSig.block();
        }

        public void setErrorCallback(Camera.ErrorCallback paramErrorCallback) {
            mSig.close();
            mCameraHandler.obtainMessage(CAMERA_MSG_SET_ERROR_CB, paramErrorCallback).sendToTarget();
            mSig.block();
        }

        public void setFaceDetectionListener(Camera.FaceDetectionListener paramFaceDetectionListener) {
            mSig.close();
            mCameraHandler.obtainMessage(CAMERA_MSG_SET_FACE_DETECTION_LISTENER, paramFaceDetectionListener).sendToTarget();
            mSig.block();
        }

        public void setOneShotPreviewCallback(Camera.PreviewCallback paramPreviewCallback) {
            mSig.close();
            mCameraHandler.obtainMessage(CAMERA_MSG_SET_ONESHOT_PREVIEW_CB, paramPreviewCallback).sendToTarget();
            mSig.block();
        }

        public void setParameters(Camera.Parameters paramParameters) {
            mSig.close();
            mCameraHandler.obtainMessage(CAMERA_MSG_SET_PARAMS, paramParameters).sendToTarget();
            mSig.block();
        }

        public void setParametersAsync(Camera.Parameters paramParameters) {
            mCameraHandler.removeMessages(CAMERA_MSG_SET_PARAMS_ASYNC);
            mCameraHandler.obtainMessage(CAMERA_MSG_SET_PARAMS_ASYNC, paramParameters).sendToTarget();
        }

        public void setPreviewCallbackWithBuffer(Camera.PreviewCallback paramPreviewCallback) {
            mSig.close();
            mCameraHandler.obtainMessage(CAMERA_MSG_SET_PREVIEW_VB_WITHBUFFER, paramPreviewCallback).sendToTarget();
            mSig.block();
        }

        public void setPreviewCallback(Camera.PreviewCallback paramPreviewCallback){
            mSig.close();
            mCameraHandler.obtainMessage(CAMERA_MSG_SET_PREVIEW_VB, paramPreviewCallback).sendToTarget();
            mSig.block();
        }

        public void addCallbackBuffer(byte[] buffer) {
            mSig.close();
            mCameraHandler.obtainMessage(CAMERA_MSG_ADD_CB_BUFFER, buffer).sendToTarget();
            mSig.block();
        }

        public void setPreviewTextureAsync(SurfaceTexture paramSurfaceTexture) {
            mCameraHandler.obtainMessage(CAMERA_MSG_SET_PREVIEW_TEXTURE_ASYNC, paramSurfaceTexture).sendToTarget();
        }

        public void setZoomChangeListener(Camera.OnZoomChangeListener paramOnZoomChangeListener) {
            mSig.close();
            mCameraHandler.obtainMessage(CAMERA_MSG_SET_ZOOM_CHANGE_LISTENER, paramOnZoomChangeListener).sendToTarget();
            mSig.block();
        }

        public void startFaceDetection() {
            mSig.close();
            mCameraHandler.sendEmptyMessage(CAMERA_MSG_START_FACE_DETECTION);
            mSig.block();
        }

        public void startPreviewAsync() {
            mCameraHandler.sendEmptyMessage(CAMERA_MSG_START_PREVIEW_ASYNC);
        }

        public void stopFaceDetection() {
            mSig.close();
            mCameraHandler.sendEmptyMessage(CAMERA_MSG_STOP_FACE_DETECTION);
            mSig.block();
        }

        public void stopPreview() {
            mSig.close();
            mCameraHandler.sendEmptyMessage(CAMERA_MSG_STOP_PREVIEW);
            mSig.block();
        }

        public void takePicture(final Camera.ShutterCallback shutter, final Camera.PictureCallback raw,
                                final Camera.PictureCallback postview, final Camera.PictureCallback jpeg) {
            mSig.close();
            mCameraHandler.post(new Runnable() {
                public void run() {
                    mCamera.takePicture(shutter, raw, postview, jpeg);
                    mSig.open();
                }
            });
            mSig.block();
        }

        public void unlock() {
            mSig.close();
            mCameraHandler.sendEmptyMessage(CAMERA_MSG_UNLOCK);
            mSig.block();
        }

        public void setPreviewTexture(SurfaceTexture previewTexture) {
            mSig.close();
            mCameraHandler.obtainMessage(CAMERA_MSG_SET_PREVIEW_TEXTURE, previewTexture).sendToTarget();
            mSig.block();
        }

        public void setPreviewDisplay(SurfaceHolder previewDisplay) {
            mSig.close();
            mCameraHandler.obtainMessage(CAMERA_MSG_SET_PREVIEW_DISPLAY, previewDisplay).sendToTarget();
            mSig.block();
        }

        public void setExceptionListener(CameraExceptionListener listener) {
            mExceptionListener = listener;
        }

        public void startPreview() {
            mSig.close();
            mCameraHandler.sendEmptyMessage(CAMERA_MSG_START_PREVIEW);
            mSig.block();
        }
    }
}

