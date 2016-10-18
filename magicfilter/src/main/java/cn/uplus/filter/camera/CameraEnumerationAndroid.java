package cn.uplus.filter.camera;

import android.graphics.ImageFormat;
import android.util.Log;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.lang.Math.abs;

@SuppressWarnings("deprecation")
public class CameraEnumerationAndroid {
  private final static String TAG = "CameraEnumeratAndroid";
  // Synchronized on |CameraEnumerationAndroid.this|.
  private static Enumerator enumerator = new CameraEnumerator();

  public interface Enumerator {
    /**
     * Returns a list of supported CaptureFormats for the camera with index |cameraId|.
     */
    List<CaptureFormat> getSupportedFormats(int cameraId);
  }

  public static synchronized void setEnumerator(Enumerator enumerator) {
    CameraEnumerationAndroid.enumerator = enumerator;
  }

  public static synchronized List<CaptureFormat> getSupportedFormats(int cameraId) {
    final List<CaptureFormat> formats = enumerator.getSupportedFormats(cameraId);
    Log.d(TAG, "Supported formats for camera " + cameraId + ": " + formats);
    return formats;
  }

  public static class CaptureFormat {
    // Class to represent a framerate range. The framerate varies because of lightning conditions.
    // The values are multiplied by 1000, so 1000 represents one frame per second.
    public static class FramerateRange {
      public int min;
      public int max;
      public FramerateRange(int min, int max) {
        this.min = min;
        this.max = max;
      }
      @Override
      public String toString() {
        return "[" + (min / 1000.0f) + ":" + (max / 1000.0f) + "]";
      }
      @Override
      public boolean equals(Object other) {
        if (!(other instanceof FramerateRange)) {
          return false;
        }
        final FramerateRange otherFramerate = (FramerateRange) other;
        return min == otherFramerate.min && max == otherFramerate.max;
      }
      @Override
      public int hashCode() {
        // Use prime close to 2^16 to avoid collisions for normal values less than 2^16.
        return 1 + 65537 * min + max;
      }
    }
    public final int width;
    public final int height;
    public final FramerateRange framerate;
    // TODO(hbos): If VideoCapturer.startCapture is updated to support other image formats then this
    // needs to be updated and VideoCapturer.getSupportedFormats need to return CaptureFormats of
    // all imageFormats.
    public final int imageFormat = ImageFormat.NV21;

    public CaptureFormat(int width, int height, int minFramerate, int maxFramerate) {
      this.width = width;
      this.height = height;
      this.framerate = new FramerateRange(minFramerate, maxFramerate);
    }

    public CaptureFormat(int width, int height, FramerateRange framerate) {
      this.width = width;
      this.height = height;
      this.framerate = framerate;
    }

    // Calculates the frame size of this capture format.
    public int frameSize() {
      return frameSize(width, height, imageFormat);
    }

    // Calculates the frame size of the specified image format. Currently only
    // supporting ImageFormat.NV21.
    // The size is width * height * number of bytes per pixel.
    // http://developer.android.com/reference/android/hardware/Camera.html#addCallbackBuffer(byte[])
    public static int frameSize(int width, int height, int imageFormat) {
      if (imageFormat != ImageFormat.NV21) {
        throw new UnsupportedOperationException("Don't know how to calculate "
            + "the frame size of non-NV21 image formats.");
      }
      return (width * height * ImageFormat.getBitsPerPixel(imageFormat)) / 8;
    }

    @Override
    public String toString() {
      return width + "x" + height + "@" + framerate;
    }

    public boolean isSameFormat(final CaptureFormat that) {
      if (that == null) {
        return false;
      }
      return width == that.width && height == that.height && framerate.equals(that.framerate);
    }
  }

  // Prefer a fps range with an upper bound close to |framerate|. Also prefer a fps range with a low
  // lower bound, to allow the framerate to fluctuate based on lightning conditions.
  public static CaptureFormat.FramerateRange getClosestSupportedFramerateRange(
          List<CaptureFormat.FramerateRange> supportedFramerates, final int requestedFps) {
    return Collections.min(supportedFramerates,
            new ClosestComparator<CaptureFormat.FramerateRange>() {
              // Progressive penalty if the upper bound is further away than |MAX_FPS_DIFF_THRESHOLD|
              // from requested.
              private static final int MAX_FPS_DIFF_THRESHOLD = 5000;
              private static final int MAX_FPS_LOW_DIFF_WEIGHT = 1;
              private static final int MAX_FPS_HIGH_DIFF_WEIGHT = 3;
              // Progressive penalty if the lower bound is bigger than |MIN_FPS_THRESHOLD|.
              private static final int MIN_FPS_THRESHOLD = 8000;
              private static final int MIN_FPS_LOW_VALUE_WEIGHT = 1;
              private static final int MIN_FPS_HIGH_VALUE_WEIGHT = 4;
              // Use one weight for small |value| less than |threshold|, and another weight above.
              private int progressivePenalty(int value, int threshold, int lowWeight, int highWeight) {
                return (value < threshold)
                        ? value * lowWeight
                        : threshold * lowWeight + (value - threshold) * highWeight;
              }
              @Override
              int diff(CaptureFormat.FramerateRange range) {
                final int minFpsError = progressivePenalty(range.min,
                        MIN_FPS_THRESHOLD, MIN_FPS_LOW_VALUE_WEIGHT, MIN_FPS_HIGH_VALUE_WEIGHT);
                final int maxFpsError = progressivePenalty(Math.abs(requestedFps * 1000 - range.max),
                        MAX_FPS_DIFF_THRESHOLD, MAX_FPS_LOW_DIFF_WEIGHT, MAX_FPS_HIGH_DIFF_WEIGHT);
                return minFpsError + maxFpsError;
              }
            });
  }

  // Returns device names that can be used to create a new VideoCapturerAndroid.
  public static String[] getDeviceNames() {
    String[] names = new String[android.hardware.Camera.getNumberOfCameras()];
    for (int i = 0; i < android.hardware.Camera.getNumberOfCameras(); ++i) {
      names[i] = getDeviceName(i);
    }
    return names;
  }

  // Returns number of cameras on device.
  public static int getDeviceCount() {
    return android.hardware.Camera.getNumberOfCameras();
  }

  // Returns the name of the camera with camera index. Returns null if the
  // camera can not be used.
  public static String getDeviceName(int index) {
    android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
    try {
      android.hardware.Camera.getCameraInfo(index, info);
    } catch (Exception e) {
      Log.e(TAG, "getCameraInfo failed on index " + index,e);
      return null;
    }

    String facing =
        (info.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) ? "front" : "back";
    return "Camera " + index + ", Facing " + facing
        + ", Orientation " + info.orientation;
  }

  // Returns the name of the front facing camera. Returns null if the
  // camera can not be used or does not exist.
  public static String getNameOfFrontFacingDevice() {
    return getNameOfDevice(android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
  }

  // Returns the name of the back facing camera. Returns null if the
  // camera can not be used or does not exist.
  public static String getNameOfBackFacingDevice() {
    return getNameOfDevice(android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK);
  }

  // Helper class for finding the closest supported format for the two functions below.
  private static abstract class ClosestComparator<T> implements Comparator<T> {
    // Difference between supported and requested parameter.
    abstract int diff(T supportedParameter);

    @Override
    public int compare(T t1, T t2) {
      return diff(t1) - diff(t2);
    }
  }

  public static int[] getFramerateRange(android.hardware.Camera.Parameters parameters,
      final int framerate) {
    List<int[]> listFpsRange = parameters.getSupportedPreviewFpsRange();
    if (listFpsRange.isEmpty()) {
      Log.w(TAG, "No supported preview fps range");
      return new int[]{0, 0};
    }
    return Collections.min(listFpsRange,
        new ClosestComparator<int[]>() {
          @Override int diff(int[] range) {
            final int maxFpsWeight = 10;
            return range[android.hardware.Camera.Parameters.PREVIEW_FPS_MIN_INDEX]
                + maxFpsWeight * abs(framerate
                    - range[android.hardware.Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
          }
     });
  }

  public static android.hardware.Camera.Size getClosestSupportedSize(
      List<android.hardware.Camera.Size> supportedSizes, final int requestedWidth,
      final int requestedHeight) {
    return Collections.min(supportedSizes,
        new ClosestComparator<android.hardware.Camera.Size>() {
          @Override int diff(android.hardware.Camera.Size size) {
            return abs(requestedWidth - size.width) + abs(requestedHeight - size.height);
          }
     });
  }

  private static String getNameOfDevice(int facing) {
    final android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
    for (int i = 0; i < android.hardware.Camera.getNumberOfCameras(); ++i) {
      try {
        android.hardware.Camera.getCameraInfo(i, info);
        if (info.facing == facing) {
          return getDeviceName(i);
        }
      } catch (Exception e) {
        Log.e(TAG, "getCameraInfo() failed on index " + i, e);
      }
    }
    return null;
  }

  public static int getClosestSupportedIntValue(final List<Integer> values, final int value){
    return Collections.min(values, new ClosestComparator<Integer>() {
      @Override
      int diff(Integer supportedParameter) {
        return abs(value - supportedParameter);
      }
    });
  }
}
