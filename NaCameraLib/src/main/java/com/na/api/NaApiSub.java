package com.na.api;

import android.content.Context;

import com.na.camera.BuildConfig;
import com.na.uitls.NaLog;

/**
 * @actor:taotao
 * @DATE: 16/10/14
 */
public class NaApiSub {
    private Context mApplicationContext;

    public void init(Context context) {
        setIsDebug(BuildConfig.DEBUG);
        setLogLevel(NaLog.DEFAULT);
        mApplicationContext = context;
    }

    public void release() {

    }

    public Context getApplicationContext() {
        return mApplicationContext;
    }

    public void setIsDebug(boolean debug) {
        NaLog.setIsDebug(debug);
    }

    public void setLogLevel(int level) {
        NaLog.setLogLevel(level);
    }
}