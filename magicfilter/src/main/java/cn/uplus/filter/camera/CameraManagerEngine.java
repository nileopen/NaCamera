package cn.uplus.filter.camera;


import android.hardware.Camera;
import android.util.Log;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by taotao on 16/7/8.
 */
public class CameraManagerEngine {
    private final static String TAG = "CameraManagerEngine";
    public final static int DEFAULT_WIDTH = 480;
    public final static int DEFAULT_HEIGHT = 640;
    public final static int DEFAULT_FRAMERATE = 20;
    //    private HandlerThread cameraThread;
//    private final Handler cameraThreadHandler;
    private Camera camera;
    private Camera.CameraInfo info;
    private CameraEventHandler eventHandler;
    private int cameraId = 0;
    private int requestedWidth = DEFAULT_WIDTH;
    private int requestedHeight = DEFAULT_HEIGHT;
    private int requestedFramerate = DEFAULT_FRAMERATE;
    private CameraEnumerationAndroid.CaptureFormat captureFormat;
    private final boolean isCapturingToTexture;
    // Arbitrary queue depth.  Higher number means more memory allocated & held,
    // lower number means more sensitivity to processing time in the client (and
    // potentially stalling the capturer if it runs out of buffers to write to).
    private static final int NUMBER_OF_CAPTURE_BUFFERS = 3;
    private final Set<byte[]> queuedBuffers = new HashSet<byte[]>();


    public CameraManagerEngine(int cameraId, int width, int height, int framerate, CameraEventHandler eventHandler) {
        this.cameraId = cameraId;
        this.requestedWidth = width;
        this.requestedHeight = height;
        this.requestedFramerate = framerate;
        this.eventHandler = eventHandler;
        isCapturingToTexture = false;

//        cameraThread = new HandlerThread(TAG);
//        cameraThread.start();
//        cameraThreadHandler = new Handler(cameraThread.getLooper());
    }

    public CameraManagerEngine(int cameraId, CameraEventHandler eventHandler) {
        this(cameraId, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_FRAMERATE, eventHandler);
    }

    private void checkIsOnCameraThread() {
//        if (Thread.currentThread() != cameraThread) {
//            throw new IllegalStateException("Wrong thread");
//        }
    }

    public boolean isDisposed() {
//        return (cameraThread == null);
        return false;
    }

    public void disposed() {
//        Log.d(TAG, "release");
//        if (isDisposed()) {
//            throw new IllegalStateException("Already released");
//        }
//        ThreadUtils.invokeUninterruptibly(cameraThreadHandler, new Runnable() {
//            @Override
//            public void run() {
//                if (camera != null) {
//                    throw new IllegalStateException("Release called while camera is running");
//                }
//            }
//        });
//        surfaceHelper.disconnect(cameraThreadHandler);
//        cameraThread = null;
    }

    public void open(int id) {
        if (camera != null) {
            return;
        }

        try {
            if (eventHandler != null) {
                eventHandler.onOpening(id);
            }
            camera = Camera.open(id);
            info = new Camera.CameraInfo();
            Camera.getCameraInfo(id, info);
            camera.setErrorCallback(new Camera.ErrorCallback() {
                @Override
                public void onError(int error, Camera camera) {
                    if (eventHandler != null) {
                        String errorInfo = "Camera error:" + error;
                        eventHandler.onError(errorInfo);
                    }
                }
            });

            setDefaultParameters();
        } catch (Exception e) {
            if (eventHandler != null) {
                eventHandler.onError(e.getMessage());
            }
        }
    }

    private void setDefaultParameters() {
        if (camera != null) {
            int framerate = requestedFramerate;
            int width = requestedWidth;
            int height = requestedHeight;
            // Find closest supported format for |width| x |height| @ |framerate|.
            Camera.Parameters parameters = camera.getParameters();
            final List<CameraEnumerationAndroid.CaptureFormat.FramerateRange> supportedFramerates =
                    CameraEnumerator.convertFramerates(parameters.getSupportedPreviewFpsRange());
            Log.d(TAG, "Available fps ranges: " + supportedFramerates);
            final CameraEnumerationAndroid.CaptureFormat.FramerateRange bestFpsRange;
            if (supportedFramerates.isEmpty()) {
                Log.w(TAG, "No supported preview fps range");
                bestFpsRange = new CameraEnumerationAndroid.CaptureFormat.FramerateRange(0, 0);
            } else {
                bestFpsRange = CameraEnumerationAndroid.getClosestSupportedFramerateRange(
                        supportedFramerates, framerate);
            }

            final Camera.Size previewSize = CameraEnumerationAndroid.getClosestSupportedSize(
                    parameters.getSupportedPreviewSizes(), width, height);
            final CameraEnumerationAndroid.CaptureFormat captureFormat = new CameraEnumerationAndroid.CaptureFormat(
                    previewSize.width, previewSize.height, bestFpsRange);

            // Check if we are already using this capture format, then we don't need to do anything.
            if (captureFormat.isSameFormat(this.captureFormat)) {
                return;
            }

            // Update camera parameters.
            Log.d(TAG, "isVideoStabilizationSupported: " + parameters.isVideoStabilizationSupported());
            if (parameters.isVideoStabilizationSupported()) {
                parameters.setVideoStabilization(true);
            }
            // Note: setRecordingHint(true) actually decrease frame rate on N5.
            // parameters.setRecordingHint(true);
            if (captureFormat.framerate.max > 0) {
                parameters.setPreviewFpsRange(captureFormat.framerate.min, captureFormat.framerate.max);
            }

            try {
                int fr = CameraEnumerationAndroid.getClosestSupportedIntValue(parameters.getSupportedPreviewFrameRates(), requestedFramerate);
                parameters.setPreviewFrameRate(fr);
            } catch (Exception e) {

            }

            parameters.setPreviewSize(captureFormat.width, captureFormat.height);

            if (!isCapturingToTexture) {
                parameters.setPreviewFormat(captureFormat.imageFormat);
            }
            // Picture size is for taking pictures and not for preview/video, but we need to set it anyway
            // as a workaround for an aspect ratio problem on Nexus 7.
            final android.hardware.Camera.Size pictureSize = CameraEnumerationAndroid.getClosestSupportedSize(
                            parameters.getSupportedPictureSizes(), width, height);
            parameters.setPictureSize(pictureSize.width, pictureSize.height);

            // Temporarily stop preview if it's already running.
//            if (this.captureFormat != null) {
//                camera.stopPreview();
//                dropNextFrame = true;
                // Calling |setPreviewCallbackWithBuffer| with null should clear the internal camera buffer
                // queue, but sometimes we receive a frame with the old resolution after this call anyway.
//                camera.setPreviewCallbackWithBuffer(null);
//            }

            // (Re)start preview.
            Log.d(TAG, "Start capturing: " + captureFormat);
            this.captureFormat = captureFormat;

            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes.contains(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                parameters.setFocusMode(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }

            camera.setParameters(parameters);
//            if (!isCapturingToTexture) {
//                queuedBuffers.clear();
//                final int frameSize = captureFormat.frameSize();
//                for (int i = 0; i < NUMBER_OF_CAPTURE_BUFFERS; ++i) {
//                    final ByteBuffer buffer = ByteBuffer.allocateDirect(frameSize);
//                    queuedBuffers.add(buffer.array());
//                    camera.addCallbackBuffer(buffer.array());
//                }
//                camera.setPreviewCallbackWithBuffer(this);
//            }
            camera.startPreview();
        }
    }

    public void close() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    public void setEventHandler(CameraEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }
}