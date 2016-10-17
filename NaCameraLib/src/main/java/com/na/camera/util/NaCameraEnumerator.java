package com.na.camera.util;

import android.hardware.Camera;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by taotao on 16/7/15.
 */
public class NaCameraEnumerator {

    public static List<FramerateRange> convertFramerates(List<int[]> list) {
        List<FramerateRange> ranges = null;
        if (list != null) {
            ranges = new ArrayList<>();
            for (int[] range : list) {
                if (range != null && range.length >= 2) {
                    FramerateRange fr = new FramerateRange(
                            range[Camera.Parameters.PREVIEW_FPS_MIN_INDEX],
                            range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
                    ranges.add(fr);
                }
            }
        }
        return ranges;
    }

    public static FramerateRange getClosestSupportedFramerateRange(List<FramerateRange> supportedFramerates, int requestedFps) {
        return Collections.min(supportedFramerates, new FramerateRangeComparator(requestedFps));
    }

    public static Camera.Size getClosestSupportedSize(List<Camera.Size> sizeList, int width, int height) {

        return Collections.min(sizeList, new SizeComparator(width, height));
    }

    public static int getClosestSupportedIntValue(List<Integer> supportedPreviewFrameRates, int framerate) {
        return Collections.min(supportedPreviewFrameRates, new IntegerClosestComparator(framerate));
    }
}
