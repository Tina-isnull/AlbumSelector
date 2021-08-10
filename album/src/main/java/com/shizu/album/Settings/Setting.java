package com.shizu.album.Settings;

import android.view.View;

import com.shizu.album.model.entity.Photo;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.IntDef;

public class Setting {
    public static int minWidth = 1;
    public static int minHeight = 1;
    public static long minSize = 1;
    public static int count = 1;
    public static int videoMinSecond = 1;
    public static int videoMaxSecond = 1;
    public static boolean useWidth;
    public static boolean showGif;


    public static void clear() {
        minWidth = 1;
        minHeight = 1;
        minSize = 1;
        count = 1;
        videoMinSecond = 1;
        videoMaxSecond = 1;
        useWidth = false;
        showGif = false;

    }


}
