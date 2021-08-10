package com.shizu.album.model.entity;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.Serializable;

/**
 * 图片item实体类
 */

public class Photo implements Serializable {
    private static final String TAG = "Photo";
    public Uri uri;//图片Uri
    public String name;//图片名称
    public String path;//图片全路径
    public String type;//图片类型
    public int width;//图片宽度
    public int height;//图片高度
    public int orientation;//图片旋转角度
    public long size;//图片文件大小，单位：Bytes
    public long duration;//视频时长，单位：毫秒
    public long time;//图片拍摄的时间戳,单位：毫秒
    public boolean selected;//是否被选中,内部使用,无需关心
    public boolean selectedOriginal;//用户选择时是否选择了原图选项

    public Photo(String name, Uri uri, String path, long time, int width, int height,int orientation, long size, long duration, String type) {
        this.name = name;
        this.uri = uri;
        this.path = path;
        this.time = time;
        this.width = width;
        this.height = height;
        this.orientation = orientation;
        this.type = type;
        this.size = size;
        this.duration = duration;
        this.selected = false;
        this.selectedOriginal = false;
    }


    @Override
    public String toString() {
        return "Photo{" +
                "name='" + name + '\'' +
                ", uri='" + uri.toString() + '\'' +
                ", path='" + path + '\'' +
                ", time=" + time + '\'' +
                ", minWidth=" + width + '\'' +
                ", minHeight=" + height +
                ", orientation=" + orientation +
                '}';
    }

}
