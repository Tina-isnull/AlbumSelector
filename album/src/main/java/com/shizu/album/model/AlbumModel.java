package com.shizu.album.model;


import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Toast;

import com.shizu.album.Settings.Setting;
import com.shizu.album.model.entity.Album;
import com.shizu.album.model.entity.AlbumItem;
import com.shizu.album.model.entity.Photo;
import com.shizu.album.utils.ConstantUtil;
import com.shizu.album.utils.StringUtils;

import androidx.core.content.PermissionChecker;


import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by lcc on 2017/12/5.
 */

public class AlbumModel {
    private static final String TAG = "AlbumModel";
    public static AlbumModel instance;
    public Album album;
    private String[] projections;

    private AlbumModel() {
        album = new Album();
    }

    public static AlbumModel getInstance() {
        if (null == instance) {
            synchronized (AlbumModel.class) {
                if (null == instance) {
                    instance = new AlbumModel();
                }
            }
        }
        return instance;
    }

    /**
     * 专辑查询
     *
     * @param context  调用查询方法的context
     * @param callBack 查询完成后的回调
     */
    public volatile boolean canRun = true;

    public void query(Context context, final CallBack callBack) {
        final Context appCxt = context.getApplicationContext();
        if (PermissionChecker.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PermissionChecker.PERMISSION_GRANTED || callBack == null) {
            Toast.makeText(appCxt, "请开启权限", Toast.LENGTH_LONG).show();
            return;
        }

        canRun = true;
        /**
         * todo rxjava编辑
         */
        new Thread(new Runnable() {
            @Override
            public void run() {
                initAlbum(appCxt);
                if (null != callBack) callBack.onAlbumResultListener();
            }
        }).start();
    }


    /**
     * 查询所有专辑以及专辑的内容
     *
     * @param context
     */
    private synchronized void initAlbum(Context context) {
        album.clear();

        boolean canReadWidth = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN;
        //查询顺序
        final String sortOrder = MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC";
        //查询路径
        Uri contentUri = MediaStore.Files.getContentUri("external");
        //查询类型中的key
        String selection = "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?" + " OR " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)";
        //查询类型中的value
        String[] selectionAllArgs =
                new String[]{String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
                        String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)};
        //查询工具（理解）
        ContentResolver contentResolver = context.getContentResolver();
        //查询结果中的字段们
        List<String> projectionList = new ArrayList<String>();
        projectionList.add(MediaStore.Files.FileColumns._ID);
        projectionList.add(MediaStore.MediaColumns.DATA);
        projectionList.add(MediaStore.MediaColumns.DISPLAY_NAME);
        projectionList.add(MediaStore.MediaColumns.DATE_MODIFIED);
        projectionList.add(MediaStore.MediaColumns.MIME_TYPE);
        projectionList.add(MediaStore.MediaColumns.SIZE);
        projectionList.add(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME);
        //图片的尺寸
        if (Setting.useWidth) {
            projectionList.add(MediaStore.MediaColumns.WIDTH);
            projectionList.add(MediaStore.MediaColumns.HEIGHT);
            projectionList.add(MediaStore.MediaColumns.ORIENTATION);
        }

        //时长
        projectionList.add(MediaStore.MediaColumns.DURATION);

        projections = projectionList.toArray(new String[0]);

        Cursor cursor = contentResolver.query(contentUri, projections, selection,
                selectionAllArgs, sortOrder);
        if (cursor == null) {
        } else if (cursor.moveToFirst()) {
            String albumAllName = "视频和图片";

            int albumNameCol = cursor.getColumnIndex(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME);
            int durationCol = cursor.getColumnIndex(MediaStore.MediaColumns.DURATION);
            int WidthCol = cursor.getColumnIndex(MediaStore.MediaColumns.WIDTH);
            int HeightCol = cursor.getColumnIndex(MediaStore.MediaColumns.HEIGHT);
            int orientationCol = cursor.getColumnIndex(MediaStore.MediaColumns.ORIENTATION);

            boolean hasTime = durationCol > 0;

            do {
                long id = cursor.getLong(0);
                String path = cursor.getString(1);
                String name = cursor.getString(2);
                long dateTime = cursor.getLong(3);
                String type = cursor.getString(4);
                long size = cursor.getLong(5);
                long duration = 0;


                if (TextUtils.isEmpty(path) || TextUtils.isEmpty(type)) {
                    continue;
                }

                if (size < Setting.minSize) {
                    continue;
                }

                boolean isVideo = type.contains(ConstantUtil.VIDEO);// 是否是视频

                int width = 0;
                int height = 0;
                int orientation = 0;
                if (isVideo) {
                    if (hasTime)
                        duration = cursor.getLong(durationCol);
                    if (duration <= Setting.videoMinSecond || duration >= Setting.videoMaxSecond) {
                        continue;
                    }
                } else {
                    if (orientationCol != -1) {
                        orientation = cursor.getInt(orientationCol);
                    }
                    if (!Setting.showGif) {
                        if (path.endsWith(ConstantUtil.GIF) || type.endsWith(ConstantUtil.GIF)) {
                            continue;
                        }
                    }
                    if (Setting.useWidth) {
                        if (canReadWidth) {
                            width = cursor.getInt(WidthCol);
                            height = cursor.getInt(HeightCol);
                        }
                        if (width == 0 || height == 0) {
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inJustDecodeBounds = true;
                            BitmapFactory.decodeFile(path, options);
                            width = options.outWidth;
                            height = options.outHeight;
                        }

                        if (orientation == 90 || orientation == 270) {
                            int temp = width;
                            width = height;
                            height = temp;
                        }

                        if (width < Setting.minWidth || height < Setting.minHeight) {
                            continue;
                        }

                    }
                }

                Uri uri = ContentUris.withAppendedId(isVideo ?
                        MediaStore.Video.Media.getContentUri("external") :
                        MediaStore.Images.Media.getContentUri("external"), id);

                //某些机型，特定情况下三方应用或用户操作删除媒体文件时，没有通知媒体库，导致媒体库表中还有其数据，但真实文件已经不存在
                File file = new File(path);
                if (!file.isFile()) {
                    continue;
                }

                Photo imageItem = new Photo(name, uri, path, dateTime, width, height, orientation
                        , size,
                        duration, type);


                // 初始化“全部”专辑
                if (album.isEmpty()) {
                    // 用第一个图片作为专辑的封面
                    album.addAlbumItem(albumAllName, "", path, uri);
                }
                // 把图片全部放进“全部”专辑
                album.getAlbumItem(albumAllName).addImageItem(imageItem);


                // 添加当前图片的专辑到专辑模型实体中
                String albumName;
                String folderPath;
                if (albumNameCol > 0) {
                    albumName = cursor.getString(albumNameCol);
                    folderPath = albumName;
                } else {
                    File parentFile = new File(path).getParentFile();
                    if (null == parentFile) {
                        continue;
                    }
                    folderPath = parentFile.getAbsolutePath();
                    albumName = StringUtils.getLastPathSegment(folderPath);
                }
                //创建相册
                album.addAlbumItem(albumName, folderPath, path, uri);
                //相册中放图片
                album.getAlbumItem(albumName).addImageItem(imageItem);
            } while (cursor.moveToNext() && canRun);
            cursor.close();
        }
    }

    /**
     * 停止查询
     */
    public void stopQuery() {
        canRun = false;
    }

    /**
     * 查询的回调
     */
    public interface CallBack {
        void onAlbumResultListener();
    }

}
