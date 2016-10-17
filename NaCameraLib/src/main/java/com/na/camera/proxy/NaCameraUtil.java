package com.na.camera.proxy;

/**
 * @actor:taotao
 * @DATE: 16/10/14
 */
public class NaCameraUtil {

    public static void Assert(boolean flag)
    {
        if (!flag) {
            throw new AssertionError();
        }
    }
}
