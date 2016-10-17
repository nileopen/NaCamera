package com.na.camera.app.filter;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @actor:taotao
 * @DATE: 16/10/12
 */
public class GlslUtil {

    private static Context mContext;

    public static void init(Context context) {
        mContext = context;
    }

    public static String readShaderFromRawResource(final int resourceId) {
        final InputStream inputStream = mContext.getResources().openRawResource(resourceId);
        final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String nextLine;
        final StringBuilder body = new StringBuilder();

        try {
            while ((nextLine = bufferedReader.readLine()) != null) {
                body.append(nextLine);
                body.append('\n');
            }
        } catch (IOException e) {
            return null;
        }
        return body.toString();
    }
}
