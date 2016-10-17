package com.na.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;

import com.na.uitls.StatusBarUtils;

public class NaBaseActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        findViews();
        registerListeners();
    }


    protected void setWindowStatusBarColor(int colorResId) {
        StatusBarUtils.setWindowStatusBarColor(this, colorResId);
    }

    protected void findViews() {

    }

    protected void registerListeners() {

    }
}
