package com.na.uitls;


import com.na.NaApi;

/**
 * @actor:taotao
 * @DATE: 16/9/3
 */
public class ResUtils {
    public static String getStringById(int strResId) {
        return NaApi.getApi().getApplicationContext().getResources().getString(strResId);
    }
}
