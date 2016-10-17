package com.na.camera;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.SystemClock;
import android.view.SurfaceHolder;

import com.na.camera.proxy.NaCameraManager;
import com.na.camera.util.CaptureFormat;
import com.na.camera.util.FramerateRange;
import com.na.camera.util.NaCameraEnumerator;
import com.na.uitls.DeviceUtil;
import com.na.uitls.NaLog;
import com.na.uitls.OrientationUtils;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @actor:taotao
 * @DATE: 16/10/14
 */
public class NaCameraEngine implements INaCameraEngine,
        Camera.PreviewCallback,
        Camera.ErrorCallback,
        NaCameraManager.CameraExceptionListener{

    private static final String TAG = "NaCameraEngine";

    private INaCameraEvent mEvent;
    private NaCameraManager.CameraProxy mCamera;
    private boolean isFirstFrameReported = false;
    private int mCameraId;
    private Camera.CameraInfo mCameraInfo;
    private INaSurfaceHelper mSurfaceHelper;
    // Remember the requested format in case we want to switch cameras.
    private int mWidth = DEFAULT_WIDTH;
    private int mHeight = DEFAULT_HEIGHT;
    private int mFrameRate = DEFAULT_FRAME_RATE;

    // The capture format will be the closest supported format to the requested format.
    private CaptureFormat mCaptureFormat;
    private boolean isCapturingToTexture;
    private boolean mDropNextFrame;

    // Arbitrary queue depth.  Higher number means more memory allocated & held,
    // lower number means more sensitivity to processing time in the client (and
    // potentially stalling the capturer if it runs out of buffers to write to).
    private static final int NUMBER_OF_CAPTURE_BUFFERS = 3;
    private final Set<byte[]> mQueuedBuffers = new HashSet<>();
    private int mCurrentZoom;

    private void close() {
        if (mCamera != null) {
            if (isCapturingToTexture) {
                mCamera.setPreviewCallback(null);
            }else {
                mCamera.setPreviewCallbackWithBuffer(null);
            }
            mCamera.stopPreview();
            mCamera.release();
        }
    }

    private void open(int cameraId) throws Throwable{
        if (mCamera != null) {
            throw new NaCameraException("Camera has already been started.");
        }

        int num = getCameraNum();
        if (num < 1) {
            throw new NaCameraException("Camera's num < 1");
        }

        this.isFirstFrameReported = false;

        int id = cameraId % getCameraNum();
        if (mEvent != null) {
            mEvent.onCameraOpening(id);
        }

        mCamera = NaCameraManager.instance().cameraOpen(id);
        mCameraId = id;

        mCameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraId, mCameraInfo);

        if (mSurfaceHelper != null) {
            SurfaceTexture sf = mSurfaceHelper.getSurfaceTexture();
            if (sf != null) {
                mCamera.setPreviewTexture(sf);
                isCapturingToTexture = true;
            }

            SurfaceHolder holder = mSurfaceHelper.getSurfaceHolder();
            if (holder != null) {
                mCamera.setPreviewDisplay(holder);
                isCapturingToTexture = false;
            }

            boolean isFace = (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT);
            int orientation = OrientationUtils.getFrameOrientation(isFace, mCameraInfo.orientation);
            mSurfaceHelper.onRotation(orientation,isFace);
        }

        mCamera.setErrorCallback(this);

        startPreview(mWidth, mHeight, mFrameRate);
    }

    private void startPreview(int width, int height, int framerate) {
        if (mCamera != null && width > 0 && height > 0 && framerate > 0) {
            final Camera.Parameters parameters = mCamera.getParameters();
            if (parameters == null) {
                return;
            }

            List<int[]> list = parameters.getSupportedPreviewFpsRange();
            final List<FramerateRange> supportedFramerates = NaCameraEnumerator.convertFramerates(list);
            final FramerateRange bestFpsRange;
            if (supportedFramerates != null && !supportedFramerates.isEmpty()) {
                bestFpsRange = NaCameraEnumerator.getClosestSupportedFramerateRange(supportedFramerates, framerate);
            } else {
                bestFpsRange = new FramerateRange(0, 0);
            }


            final Camera.Size previewSize = NaCameraEnumerator.getClosestSupportedSize(parameters.getSupportedPreviewSizes(), width, height);
            if (previewSize == null) {
                return;
            }

            final CaptureFormat captureFormat = new CaptureFormat(previewSize.width, previewSize.height, bestFpsRange);
            // Check if we are already using this capture format, then we don't need to do anything.
            if (captureFormat.equals(this.mCaptureFormat)) {
                return;
            }

            // Update camera parameters.
            if (DeviceUtil.checkBuildVersion(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)) {
                if (parameters.isVideoStabilizationSupported()) {
                    parameters.setVideoStabilization(true);
                }
            }

            // Note: setRecordingHint(true) actually decrease frame rate on N5.
            // parameters.setRecordingHint(true);
            if (captureFormat.framerate.max > 0) {
                parameters.setPreviewFpsRange(captureFormat.framerate.min, captureFormat.framerate.max);
            }

            try {
                int fr = NaCameraEnumerator.getClosestSupportedIntValue(parameters.getSupportedPreviewFrameRates(), framerate);
                parameters.setPreviewFrameRate(fr);
            } catch (Throwable e) {
                NaLog.w(TAG, "find framerate error", e);
            }

            parameters.setPreviewSize(captureFormat.width, captureFormat.height);

            if (!isCapturingToTexture) {
                parameters.setPreviewFormat(captureFormat.imageFormat);
            }

            // Picture size is for taking pictures and not for preview/video, but we need to set it anyway
            // as a workaround for an aspect ratio problem on Nexus 7.
            final Camera.Size pictureSize = NaCameraEnumerator.getClosestSupportedSize(parameters.getSupportedPictureSizes(), width, height);
            if (pictureSize != null) {
                parameters.setPictureSize(pictureSize.width, pictureSize.height);
            }

            // Temporarily stop preview if it's already running.
            if (this.mCaptureFormat != null) {
                mCamera.stopPreview();
                mDropNextFrame = true;
                // Calling |setPreviewCallbackWithBuffer| with null should clear the internal camera buffer
                // queue, but sometimes we receive a frame with the old resolution after this call anyway.
                if (!isCapturingToTexture) {
                    mCamera.setPreviewCallbackWithBuffer(null);
                } else {
                    mCamera.setPreviewCallback(null);
                }
            }

            // (Re)start preview.
            NaLog.d(TAG, "Start capturing: " + captureFormat);
            this.mCaptureFormat = captureFormat;

            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }

            mCamera.setParameters(parameters);
            mCamera.setDisplayOrientation(90);
            if (!isCapturingToTexture) {
                mQueuedBuffers.clear();
                final int frameSize = captureFormat.frameSize();
                for (int i = 0; i < NUMBER_OF_CAPTURE_BUFFERS; ++i) {
                    final ByteBuffer buffer = ByteBuffer.allocateDirect(frameSize);
                    mQueuedBuffers.add(buffer.array());
                    mCamera.addCallbackBuffer(buffer.array());
                }
                mCamera.setPreviewCallbackWithBuffer(this);
            } else {
                mCamera.setPreviewCallback(this);
            }

            mCamera.startPreview();
        }
    }

    private int getCameraNum() {
        return NaCameraManager.getNumberOfCameras();
    }

    @Override
    public void setCameraEvent(INaCameraEvent event) {
        mEvent = event;
    }

    @Override
    public void openCamera(int cameraId) {
        try {
            open(cameraId);
        } catch (Throwable e) {
            NaLog.e(TAG, "openCamera failed", e);
            if (mEvent != null) {
                mEvent.onCameraError("openCamera failed");
            }
        }
    }

    @Override
    public void switchCamera() {
        try {
            int num = getCameraNum();
            if (num > 1) {
                closeCamera();
                mDropNextFrame = true;
                openCamera(mCameraId + 1);
                if (mEvent != null) {
                    mEvent.onSwicthCamera(true, mCameraId);
                    return;
                }
            }
        } catch (Throwable e) {
            NaLog.e(TAG, "switchCamera failed", e);
        }
        if (mEvent != null) {
            mEvent.onSwicthCamera(false, mCameraId);
        }
    }

    @Override
    public void closeCamera() {
        try {
            close();
        } catch (Throwable e) {
            NaLog.e(TAG, "closeCamera failed", e);
            if (mEvent != null) {
                mEvent.onCameraClosed();
            }
        }
        mCamera = null;
    }

    @Override
    public void switchLight(boolean isOpen) {
        if (mCamera != null && mCameraId != Camera.CameraInfo.CAMERA_FACING_FRONT) {
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                if (parameters != null) {

                    if (isOpen) {
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    } else {
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    }
                    mCamera.setParameters(parameters);
                    if (mEvent != null) {
                        mEvent.onSwithcLight(true);
                        return;
                    }
                }
            } catch (Throwable e) {
                NaLog.e(TAG, "switchLight failed", e);
            }
        }

        if (mEvent != null) {
            mEvent.onSwithcLight(false);
        }
    }

    @Override
    public void destroy() {
        closeCamera();
        mEvent = null;
    }

    @Override
    public void setSurfaceHelper(INaSurfaceHelper helper) {
        mSurfaceHelper = helper;
    }

    @Override
    public void setZoom(float scale) {
        boolean isSuccess = false;
        if (mCamera != null) {
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                if (parameters.isZoomSupported()) {
                    int maxZoom = parameters.getMaxZoom();
                    int curZoom = parameters.getZoom();

                    if (scale > 1.0f) {
                        curZoom++;
                    } else if (scale < 1.0f) {
                        curZoom--;
                    }
                    if (curZoom < 0) {
                        curZoom = 0;
                    }
                    if (curZoom > maxZoom) {
                        curZoom = maxZoom;
                    }
                    parameters.setZoom(curZoom);
                    mCamera.setParameters(parameters);
                    mCurrentZoom = curZoom;
                    isSuccess = true;
                }
            } catch (Throwable e) {
                NaLog.e(TAG, "setZoom e=" + e);
            }
        }

        if (mEvent != null) {
            mEvent.onZoom(mCurrentZoom, isSuccess);
        }
    }

    @Override
    public void setFocus() {

    }

    @Override
    public void onError(int i, Camera camera) {

    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (mCamera == null || (!mQueuedBuffers.contains(data) && isCapturingToTexture)) {
            // The camera has been stopped or |data| is an old invalid buffer.
            return;
        }

        if (camera != mCamera.getCamera()) {
            throw new RuntimeException("Unexpected camera in callback!");
        }

        final long captureTimeNs = TimeUnit.MILLISECONDS.toNanos(SystemClock.elapsedRealtime());

        if (mEvent != null && !isFirstFrameReported) {
            mEvent.onFirstFrameAvailable();
            isFirstFrameReported = true;
        }

//        cameraStatistics.addFrame();
//        frameObserver.onByteBufferFrameCaptured(data, captureFormat.width, captureFormat.height,
//                getFrameOrientation(), captureTimeNs);
        if (mSurfaceHelper != null) {
            int width = mCaptureFormat.width;
            int height = mCaptureFormat.height;
            boolean isFace = (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT);
            int orientation = OrientationUtils.getFrameOrientation(isFace, mCameraInfo.orientation);
            mSurfaceHelper.onPreviewFrame(data, width, height, orientation, captureTimeNs);
        }
        mCamera.addCallbackBuffer(data);
    }

    @Override
    public void onException(Throwable e) {
        if (mEvent != null){
            mEvent.onCameraError(e.getMessage());
        }
    }
}
