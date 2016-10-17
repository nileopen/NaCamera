package com.na.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * @actor:taotao
 * @DATE: 16/10/12
 */
public class NaAspectRatioFrameLayout extends FrameLayout {
    private int mAspectRatioWidth = 480;
    private int mAspectRatioHeight = 640;
    private boolean isFullScrean = false;

    public NaAspectRatioFrameLayout(Context context) {
        super(context);
    }

    public NaAspectRatioFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NaAspectRatioFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int originalWidth = MeasureSpec.getSize(widthMeasureSpec);
        int originalHeight = MeasureSpec.getSize(heightMeasureSpec);

        int calculatedHeight = originalWidth * mAspectRatioHeight / mAspectRatioWidth;
        int finalWidth, finalHeight;

        if (isFullScrean) {
            if (calculatedHeight < originalHeight) {
                finalWidth = originalHeight * mAspectRatioWidth / mAspectRatioHeight;
                finalHeight = originalHeight;
            } else {
                finalWidth = originalWidth;
                finalHeight = calculatedHeight;
            }
        } else {
            if (calculatedHeight > originalHeight) {
                finalWidth = originalHeight * mAspectRatioWidth / mAspectRatioHeight;
                finalHeight = originalHeight;
            } else {
                finalWidth = originalWidth;
                finalHeight = calculatedHeight;
            }
        }

        super.onMeasure(MeasureSpec.makeMeasureSpec(finalWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.EXACTLY));
    }

    public int getAspectRatioWidth() {
        return mAspectRatioWidth;
    }

    public void setAspectRatioWidth(int mAspectRatioWidth) {
        this.mAspectRatioWidth = mAspectRatioWidth;
    }

    public int getAspectRatioHeight() {
        return mAspectRatioHeight;
    }

    public void setAspectRatioHeight(int mAspectRatioHeight) {
        this.mAspectRatioHeight = mAspectRatioHeight;
    }

    public boolean isFullScrean() {
        return isFullScrean;
    }

    public void setFullScrean(boolean fullScrean) {
        isFullScrean = fullScrean;
    }
}

