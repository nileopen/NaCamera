package com.na.base;

import android.app.Application;

import com.na.NaApi;


public class NaApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        NaApi.init(this);
    }
}
