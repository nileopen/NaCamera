package com.na.camera.app.ui;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;

import com.na.base.NaBaseActivity;
import com.na.camera.INaCameraEvent;
import com.na.camera.app.R;
import com.na.camera.app.filter.GPUImageBeautyFilter;
import com.na.uitls.NaLog;
import com.na.view.NaSurfaceRender;

public class CameraActivity extends NaBaseActivity {

    private static final String TAG = "CameraActivty";
    boolean isLightOn = false;
    private GLSurfaceView sfCamera;
    private NaSurfaceRender mCameraReander;
    private ScaleGestureDetector mScaleGestureDetector = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        sfCamera = (GLSurfaceView) findViewById(R.id.sfCamera);
        mCameraReander = new NaSurfaceRender(new GPUImageBeautyFilter());
        mCameraReander.setmCameraId(0);
        mCameraReander.setCameraEvent(new INaCameraEvent() {
            @Override
            public void onCameraError(String errMsg) {
                NaLog.e(TAG, "onCameraError msg=" + errMsg);
            }

            @Override
            public void onCameraOpening(int id) {
                NaLog.e(TAG, "onCameraOpening id=" + id);
            }

            @Override
            public void onSwicthCamera(boolean isSuccess, int id) {
                NaLog.e(TAG, "onSwicthCamera id=" + id + ",isSuc=" + isSuccess);
            }

            @Override
            public void onCameraClosed() {
                NaLog.e(TAG, "onCameraClosed");
            }

            @Override
            public void onSwithcLight(boolean isSuccess) {
                NaLog.e(TAG, "onSwithcLight isSuc=" + isSuccess);
            }

            @Override
            public void onZoom(int mCurrentZoom, boolean isSuccess) {
                NaLog.e(TAG, "onZoom isSuccess=" + isSuccess + ",zoom=" + mCurrentZoom);
            }

            @Override
            public void onFirstFrameAvailable() {
                NaLog.e(TAG, "onFirstFrameAvailable");
            }
        });
        mCameraReander.setGLSurfaceView(sfCamera);
        initButtons();
        initListener();
    }

    private void initListener() {
        mScaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float cs = detector.getCurrentSpan();
                float csX = detector.getCurrentSpanX();
                float csY = detector.getCurrentSpanY();

                long et = detector.getEventTime();
                float fx = detector.getFocusX();
                float fy = detector.getFocusY();
                float ps = detector.getPreviousSpan();
                float psX = detector.getPreviousSpanX();
                float psY = detector.getPreviousSpanY();

                NaLog.e(TAG, "onScale cs=" + cs + ",csX=" + csX + ",csY=" + csY + ",et=" + et
                        + ",fx=" + fx + ",fy=" + fy + ",ps=" + ps + ",psX" + psX + ",psY" + psY);
                float sf = detector.getScaleFactor();
                long td = detector.getTimeDelta();
                NaLog.e(TAG, "onScale sf=" + sf + ",td=" + td);
                if (mCameraReander != null) {
                    mCameraReander.setZoom(sf);
                }
                return false;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                float cs = detector.getCurrentSpan();
                float csX = detector.getCurrentSpanX();
                float csY = detector.getCurrentSpanY();

                long et = detector.getEventTime();
                float fx = detector.getFocusX();
                float fy = detector.getFocusY();
                float ps = detector.getPreviousSpan();
                float psX = detector.getPreviousSpanX();
                float psY = detector.getPreviousSpanY();
                NaLog.e(TAG, "onScaleBegin cs=" + cs + ",csX=" + csX + ",csY=" + csY + ",et=" + et
                        + ",fx=" + fx + ",fy=" + fy + ",ps=" + ps + ",psX" + psX + ",psY" + psY);
                float sf = detector.getScaleFactor();
                long td = detector.getTimeDelta();
                NaLog.e(TAG, "onScaleBegin sf=" + sf + ",td=" + td);
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                float cs = detector.getCurrentSpan();
                float csX = detector.getCurrentSpanX();
                float csY = detector.getCurrentSpanY();

                long et = detector.getEventTime();
                float fx = detector.getFocusX();
                float fy = detector.getFocusY();
                float ps = detector.getPreviousSpan();
                float psX = detector.getPreviousSpanX();
                float psY = detector.getPreviousSpanY();
                NaLog.e(TAG, "onScaleEnd cs=" + cs + ",csX=" + csX + ",csY=" + csY + ",et=" + et
                        + ",fx=" + fx + ",fy=" + fy + ",ps=" + ps + ",psX" + psX + ",psY" + psY);
                float sf = detector.getScaleFactor();
                long td = detector.getTimeDelta();
                NaLog.e(TAG, "onScaleEnd sf=" + sf + ",td=" + td);
                if (mCameraReander != null) {
                    mCameraReander.setFocus();
                }
            }
        });

        sfCamera.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mScaleGestureDetector.onTouchEvent(event);
            }
        });
    }

    private void initButtons() {
        Button btSwitch = (Button) findViewById(R.id.btSwitch);
        btSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCameraReander != null) {
                    mCameraReander.switchCamera();
                }
            }
        });

        Button btLigth = (Button) findViewById(R.id.btLight);
        btLigth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLightOn = !isLightOn;
                if (mCameraReander != null) {
                    mCameraReander.switchLight(isLightOn);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCameraReander != null) {
            mCameraReander.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCameraReander != null) {
            mCameraReander.onPause();
        }
    }
}
