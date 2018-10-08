package com.sanvar.qiqiaoban;

import android.graphics.Bitmap;
import android.util.Log;

public class ImgInfo {
    public int x, y;
    public Bitmap bm;
    public int bx, by;

    private static final String TAG = "ImgInfo";

    public ImgInfo(int x, int y, Bitmap bm) {
        this.x = x;
        this.y = y;
        this.bm = bm;
        this.bx = x;
        this.by = y;
    }

    public boolean valid() {
        return (x == bx && y == by);
    }
}
