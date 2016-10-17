package com.na.camera.app.filter;

import android.opengl.GLES20;

import com.na.camera.app.R;
import com.na.filter.GPUImageFilter;


/**
 * @actor:taotao
 * @DATE: 16/10/12
 */
public class GPUImageBeautyFilter extends GPUImageFilter {
    public final static int BEAUTY_LEVEL_1 = 1;
    public final static int BEAUTY_LEVEL_2 = 2;
    public final static int BEAUTY_LEVEL_3 = 3;
    public final static int BEAUTY_LEVEL_4 = 4;
    public final static int BEAUTY_LEVEL_5 = 5;

    private int mSingleStepOffsetLocation;
    private int mParamsLocation;

    public GPUImageBeautyFilter() {
        super(NO_FILTER_VERTEX_SHADER, GlslUtil.readShaderFromRawResource(R.raw.beauty));
    }

    @Override
    public void onInit() {
        super.onInit();
        mSingleStepOffsetLocation = GLES20.glGetUniformLocation(getProgram(), "singleStepOffset");
        mParamsLocation = GLES20.glGetUniformLocation(getProgram(), "params");
        setBeautyLevel(BEAUTY_LEVEL_3);
    }

    private void setBeautyLevel(int level) {
        switch (level) {
            case BEAUTY_LEVEL_1: {
                setFloat(mParamsLocation, 1.0f);
                break;
            }
            case BEAUTY_LEVEL_2: {
                setFloat(mParamsLocation, 0.8f);
                break;
            }
            case BEAUTY_LEVEL_3: {
                setFloat(mParamsLocation, 0.6f);
                break;
            }
            case BEAUTY_LEVEL_4: {
                setFloat(mParamsLocation, 0.4f);
                break;
            }
            case BEAUTY_LEVEL_5: {
                setFloat(mParamsLocation, 0.33f);
                break;
            }
            default: {
                setFloat(mParamsLocation, 0.0f);
                break;
            }
        }
    }

    private void setTexelSize(final float w, final float h) {
        setFloatVec2(mSingleStepOffsetLocation, new float[] {2.0f / w, 2.0f / h});
    }

    @Override
    public void onOutputSizeChanged(int width, int height) {
        super.onOutputSizeChanged(width, height);
        setTexelSize(width, height);
    }
}
