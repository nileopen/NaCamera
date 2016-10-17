package com.na.view;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.view.SurfaceHolder;

import com.na.NaApi;
import com.na.camera.INaCameraEvent;
import com.na.camera.INaSurfaceHelper;
import com.na.camera.NaCameraEngineManager;
import com.na.filter.GPUImageFilter;
import com.na.filter.OpenGlUtils;
import com.na.filter.Rotation;
import com.na.filter.ScaleType;
import com.na.filter.TextureRotationUtil;
import com.na.jni.JniCommonUtil;
import com.na.uitls.NaLog;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.na.filter.TextureRotationUtil.CUBE;
import static com.na.filter.TextureRotationUtil.TEXTURE_NO_ROTATION;

/**
 * @actor:taotao
 * @DATE: 16/10/15
 */
public class NaSurfaceRender implements GLSurfaceView.Renderer, SurfaceHolder.Callback, INaSurfaceHelper{

    private static final String TAG = "NaSurfaceRender";
    public static final int NO_IMAGE = -1;

    private GLSurfaceView mGLSurfaceView;
    private GPUImageFilter mFilter;

    private final Queue<Runnable> mRunOnDraw;
    private final Queue<Runnable> mRunOnDrawEnd;
    private Rotation mRotation;
    private boolean mFlipHorizontal;
    private boolean mFlipVertical;

    private final FloatBuffer mGLCubeBuffer;
    private final FloatBuffer mGLTextureBuffer;
    private IntBuffer mGLRgbBuffer;

    private int mOutputWidth;
    private int mOutputHeight;
    private int mImageWidth;
    private int mImageHeight;

    private ScaleType mScaleType = ScaleType.CENTER_CROP;

    private float mBackgroundRed = 0;
    private float mBackgroundGreen = 0;
    private float mBackgroundBlue = 0;

    public final Object mSurfaceChangedWaiter = new Object();

    private int mGLTextureId = NO_IMAGE;

    private SurfaceTexture mSurfaceTexture = null;
    //default front
    private int mCameraId = 1;

    public NaSurfaceRender(GPUImageFilter filter) {
        if (!supportsOpenGLES2()) {
            throw new IllegalStateException("OpenGL ES 2.0 is not supported on this phone.");
        }

        this.mFilter = filter;
        mRunOnDraw = new LinkedList<Runnable>();
        mRunOnDrawEnd = new LinkedList<Runnable>();

        mGLCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLCubeBuffer.put(CUBE).position(0);

        mGLTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        setRotation(Rotation.NORMAL, false, false);
    }

    /**
     * Checks if OpenGL ES 2.0 is supported on the current device.
     *
     * @return true, if successful
     */
    private boolean supportsOpenGLES2() {
        Context context = NaApi.getApi().getApplicationContext();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        return configurationInfo.reqGlEsVersion >= 0x20000;
    }

    public void setRotation(final Rotation rotation, final boolean flipHorizontal, final boolean flipVertical) {
        mFlipHorizontal = flipHorizontal;
        mFlipVertical = flipVertical;
        setRotation(rotation);
    }

    public void setRotation(final Rotation rotation) {
        mRotation = rotation;
        adjustImageScaling();
    }

    private void adjustImageScaling() {
        float outputWidth = mOutputWidth;
        float outputHeight = mOutputHeight;
        if (mRotation == Rotation.ROTATION_270 || mRotation == Rotation.ROTATION_90) {
            outputWidth = mOutputHeight;
            outputHeight = mOutputWidth;
        }

        float ratio1 = outputWidth / mImageWidth;
        float ratio2 = outputHeight / mImageHeight;
        float ratioMax = Math.max(ratio1, ratio2);
        int imageWidthNew = Math.round(mImageWidth * ratioMax);
        int imageHeightNew = Math.round(mImageHeight * ratioMax);

        float ratioWidth = imageWidthNew / outputWidth;
        float ratioHeight = imageHeightNew / outputHeight;

        float[] cube = CUBE;
        float[] textureCords = TextureRotationUtil.getRotation(mRotation, mFlipHorizontal, mFlipVertical);
        if (mScaleType == ScaleType.CENTER_CROP) {
            float distHorizontal = (1 - 1 / ratioWidth) / 2;
            float distVertical = (1 - 1 / ratioHeight) / 2;
            textureCords = new float[]{
                    addDistance(textureCords[0], distHorizontal), addDistance(textureCords[1], distVertical),
                    addDistance(textureCords[2], distHorizontal), addDistance(textureCords[3], distVertical),
                    addDistance(textureCords[4], distHorizontal), addDistance(textureCords[5], distVertical),
                    addDistance(textureCords[6], distHorizontal), addDistance(textureCords[7], distVertical),
            };
        } else {
            cube = new float[]{
                    CUBE[0] / ratioHeight, CUBE[1] / ratioWidth,
                    CUBE[2] / ratioHeight, CUBE[3] / ratioWidth,
                    CUBE[4] / ratioHeight, CUBE[5] / ratioWidth,
                    CUBE[6] / ratioHeight, CUBE[7] / ratioWidth,
            };
        }

        mGLCubeBuffer.clear();
        mGLCubeBuffer.put(cube).position(0);
        mGLTextureBuffer.clear();
        mGLTextureBuffer.put(textureCords).position(0);
    }

    private float addDistance(float coordinate, float distance) {
        return coordinate == 0.0f ? distance : 1 - distance;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        NaLog.d(TAG, "onSurfaceCreated");
        GLES20.glClearColor(mBackgroundRed, mBackgroundGreen, mBackgroundBlue, 1);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        mFilter.init();
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        NaLog.d(TAG, "onSurfaceChanged width=" + width + ",height=" + height);
        mOutputWidth = width;
        mOutputHeight = height;
        GLES20.glViewport(0, 0, width, height);
        GLES20.glUseProgram(mFilter.getProgram());
        mFilter.onOutputSizeChanged(width, height);
        adjustImageScaling();
        synchronized (mSurfaceChangedWaiter) {
            mSurfaceChangedWaiter.notifyAll();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onDrawFrame(GL10 gl10) {
        NaLog.d(TAG, "onDrawFrame");
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        runAll(mRunOnDraw);
        mFilter.onDraw(mGLTextureId, mGLCubeBuffer, mGLTextureBuffer);
        runAll(mRunOnDrawEnd);
        if (mSurfaceTexture != null) {
            mSurfaceTexture.updateTexImage();
        }
    }

    private void runAll(Queue<Runnable> queue) {
        synchronized (queue) {
            while (!queue.isEmpty()) {
                queue.poll().run();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        NaLog.d(TAG, "surfaceCreated");
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width,int height) {
        NaLog.d(TAG, "surfaceChanged format=" + format + ",width=" + width + ",height=" + height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        NaLog.d(TAG, "surfaceDestroyed");
    }

    @Override
    public SurfaceTexture getSurfaceTexture() {
//        return mSurfaceTexture;
        return mSurfaceTexture;
    }

    @Override
    public SurfaceHolder getSurfaceHolder() {
        return null;
//        return mGLSurfaceView.getHolder();
    }

    @Override
    public void onPreviewFrame(final byte[] data, final int width, final int height, int orientation, long captureTimeNs) {
        NaLog.e(TAG, "onPreviewFrame width=" + width + ",height=" + height + ",orientation=" + orientation + ",captureTimeNs=" + captureTimeNs);
        if (mGLRgbBuffer == null) {
            mGLRgbBuffer = IntBuffer.allocate(width * height);
        }

        if (mRunOnDraw.isEmpty()) {
            runOnDraw(new Runnable() {
                @Override
                public void run() {
                    JniCommonUtil.getInstance().YUVtoRBGA(data, width, height, mGLRgbBuffer.array());
                    mGLTextureId = OpenGlUtils.loadTexture(mGLRgbBuffer, width, height, mGLTextureId);
                    if (mImageWidth != width) {
                        mImageWidth = width;
                        mImageHeight = height;
                        adjustImageScaling();
                    }
                }
            });
        }
    }

    @Override
    public void onRotation(int orientation, boolean isFace) {
        NaLog.e(TAG, "onRotation orientation=" + orientation + ",isFace=" + isFace);
        setRotation(Rotation.fromInt(orientation), isFace, false);
    }

    protected void runOnDraw(final Runnable runnable) {
        synchronized (mRunOnDraw) {
            mRunOnDraw.add(runnable);
        }
    }

    private void setUpSurfaceTexture() {
        runOnDraw(new Runnable() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void run() {
                int[] textures = new int[1];
                GLES20.glGenTextures(1, textures, 0);
                mSurfaceTexture = new SurfaceTexture(textures[0]);
                NaCameraEngineManager.getInstance().setSurfaceHelper(NaSurfaceRender.this);
                NaCameraEngineManager.getInstance().openCamera(mCameraId);
            }
        });
    }

    public void setFilter(final GPUImageFilter filter) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                final GPUImageFilter oldFilter = mFilter;
                mFilter = filter;
                if (oldFilter != null) {
                    oldFilter.destroy();
                }
                mFilter.init();
                GLES20.glUseProgram(mFilter.getProgram());
                mFilter.onOutputSizeChanged(mOutputWidth, mOutputHeight);
            }
        });
    }

    public void setGLSurfaceView(GLSurfaceView view){
        this.mGLSurfaceView = view;
        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mGLSurfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);
        mGLSurfaceView.setRenderer(this);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mGLSurfaceView.requestRender();
    }

    public void onResume(){
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        setUpSurfaceTexture();
    }

    public void setCameraEvent(INaCameraEvent event){
        NaCameraEngineManager.getInstance().setCameraEvent(event);
    }

    public void onPause(){
        NaCameraEngineManager.getInstance().closeCamera();
    }

    public void setmCameraId(int mCameraId) {
        this.mCameraId = mCameraId;
    }

    public void switchCamera(){
        NaCameraEngineManager.getInstance().switchCamera();
    }

    public void switchLight(boolean isLightOn) {
        NaCameraEngineManager.getInstance().switchLight(isLightOn);
    }

    public void setFocus() {
        NaCameraEngineManager.getInstance().setFocus();
    }

    public void setZoom(float scale) {
        NaCameraEngineManager.getInstance().setZoom(scale);
    }
}
