package com.na.camera.util;

import java.util.Comparator;

/**
 * Created by taotao on 16/7/15.
 */
public abstract class ClosestComparator<T> implements Comparator<T> {
    @Override
    public int compare(T t1, T t2) {
        return diff(t1) - diff(t2);
    }

    public abstract int diff(T t1);
}
