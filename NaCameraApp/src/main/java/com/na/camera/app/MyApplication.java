package com.na.camera.app;

import com.na.NaApi;
import com.na.base.NaApplication;
import com.na.camera.app.filter.GlslUtil;

/**
 * @actor:taotao
 * @DATE: 16/10/14
 */
public class MyApplication extends NaApplication{
    @Override
    public void onCreate() {
        super.onCreate();
        GlslUtil.init(this);
        NaApi.setIsDebug(true);
    }
}
