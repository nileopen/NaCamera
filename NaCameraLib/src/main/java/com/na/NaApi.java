package com.na;

import android.content.Context;

import com.na.api.NaApiSub;

/**
 * @actor:taotao
 * @DATE: 16/10/14
 */
public class NaApi {
    private static NaApiSub sApi;

    public static void init(Context context) {
        if (sApi == null) {
            synchronized (NaApi.class) {
                if (sApi == null) {
                    sApi = new NaApiSub();
                    sApi.init(context);
                }
            }
        }
    }

    public static void release() {
        if (sApi != null) {
            sApi.release();
        }
        sApi = null;
    }

    public static void setIsDebug(boolean debug) {
        if (sApi != null){
            sApi.setIsDebug(debug);
        }
    }

    public static void setLogLevel(int level) {
        if (sApi != null){
            sApi.setLogLevel(level);
        }
    }

    public static NaApiSub getApi() {
        return sApi;
    }
}