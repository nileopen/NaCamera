package cn.uplus.filter.camera;

import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class CameraEnumerator implements CameraEnumerationAndroid.Enumerator {
    private final static String TAG = "CameraEnumerator";
    // Each entry contains the supported formats for corresponding camera index. The formats for all
    // cameras are enumerated on the first call to getSupportedFormats(), and cached for future
    // reference.
    private List<List<CameraEnumerationAndroid.CaptureFormat>> cachedSupportedFormats;

    @Override
    public List<CameraEnumerationAndroid.CaptureFormat> getSupportedFormats(int cameraId) {
        synchronized (this) {
            if (cachedSupportedFormats == null) {
                cachedSupportedFormats = new ArrayList<List<CameraEnumerationAndroid.CaptureFormat>>();
                for (int i = 0; i < CameraEnumerationAndroid.getDeviceCount(); ++i) {
                    cachedSupportedFormats.add(enumerateFormats(i));
                }
            }
        }
        return cachedSupportedFormats.get(cameraId);
    }

    private List<CameraEnumerationAndroid.CaptureFormat> enumerateFormats(int cameraId) {
        Log.d(TAG, "Get supported formats for camera index " + cameraId + ".");
        final long startTimeMs = SystemClock.elapsedRealtime();
        final android.hardware.Camera.Parameters parameters;
        android.hardware.Camera camera = null;
        try {
            Log.d(TAG, "Opening camera with index " + cameraId);
            camera = android.hardware.Camera.open(cameraId);
            parameters = camera.getParameters();
        } catch (RuntimeException e) {
            Log.e(TAG, "Open camera failed on camera index " + cameraId, e);
            return new ArrayList<CameraEnumerationAndroid.CaptureFormat>();
        } finally {
            if (camera != null) {
                camera.release();
            }
        }

        final List<CameraEnumerationAndroid.CaptureFormat> formatList = new ArrayList<CameraEnumerationAndroid.CaptureFormat>();
        try {
            int minFps = 0;
            int maxFps = 0;
            final List<int[]> listFpsRange = parameters.getSupportedPreviewFpsRange();
            if (listFpsRange != null) {
                // getSupportedPreviewFpsRange() returns a sorted list. Take the fps range
                // corresponding to the highest fps.
                final int[] range = listFpsRange.get(listFpsRange.size() - 1);
                minFps = range[android.hardware.Camera.Parameters.PREVIEW_FPS_MIN_INDEX];
                maxFps = range[android.hardware.Camera.Parameters.PREVIEW_FPS_MAX_INDEX];
            }
            for (android.hardware.Camera.Size size : parameters.getSupportedPreviewSizes()) {
                formatList.add(new CameraEnumerationAndroid.CaptureFormat(size.width, size.height, minFps, maxFps));
            }
        } catch (Exception e) {
            Log.e(TAG, "getSupportedFormats() failed on camera index " + cameraId, e);
        }

        final long endTimeMs = SystemClock.elapsedRealtime();
        Log.d(TAG, "Get supported formats for camera index " + cameraId + " done."
                + " Time spent: " + (endTimeMs - startTimeMs) + " ms.");
        return formatList;
    }

    // Convert from int[2] to CaptureFormat.FramerateRange.
    public static List<CameraEnumerationAndroid.CaptureFormat.FramerateRange> convertFramerates(
            List<int[]> arrayRanges) {
        final List<CameraEnumerationAndroid.CaptureFormat.FramerateRange> ranges = new ArrayList<CameraEnumerationAndroid.CaptureFormat.FramerateRange>();
        for (int[] range : arrayRanges) {
            ranges.add(new CameraEnumerationAndroid.CaptureFormat.FramerateRange(
                    range[android.hardware.Camera.Parameters.PREVIEW_FPS_MIN_INDEX],
                    range[android.hardware.Camera.Parameters.PREVIEW_FPS_MAX_INDEX]));
        }
        return ranges;
    }
}
