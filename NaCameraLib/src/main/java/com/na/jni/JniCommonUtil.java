package com.na.jni;

import com.na.uitls.NaLog;

/**
 * @actor:taotao
 * @DATE: 16/10/14
 */
public class JniCommonUtil {
    private static final String TAG = "JniCommonUtil";
    private static JniCommonUtil sInstance = new JniCommonUtil();

    private native void nativeYUVtoRBGA(byte[] yuv, int width, int height, int[] out);
    private native void nativeYUVtoARBG(byte[] yuv, int width, int height, int[] out);
    static {
        loadLib();
    }

    private static void loadLib() {
        try {
            System.loadLibrary("nacommon");
        }catch (Throwable e){
            NaLog.e(TAG, "loadlib failed", e);
        }
    }

    public void YUVtoRBGA(byte[] yuv, int width, int height, int[] out){
        nativeYUVtoRBGA(yuv, width, height, out);
    }

    public void YUVtoARBG(byte[] yuv, int width, int height, int[] out){
        nativeYUVtoARBG(yuv, width, height, out);
    }

    public static JniCommonUtil getInstance() {
        return sInstance;
    }
}
