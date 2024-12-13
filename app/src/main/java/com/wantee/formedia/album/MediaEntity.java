package com.wantee.formedia.album;

import android.database.Cursor;

import androidx.annotation.NonNull;

public class MediaEntity {
    public int id;
    public int width;
    public int height;
    public long duration;
    public String path;
    public String type;
    public long size;
    public String bucketId;
    public String bucketName;

    public void read(Cursor data) {
        if (data == null) {
            return;
        }
        id = data.getInt(data.getColumnIndexOrThrow(LoaderType.FILE_PROJECTION[0]));
        width = data.getInt(data.getColumnIndexOrThrow(LoaderType.FILE_PROJECTION[1]));
        height = data.getInt(data.getColumnIndexOrThrow(LoaderType.FILE_PROJECTION[2]));
        // 使用duration获取的时长不准确
        duration = data.getLong(data.getColumnIndexOrThrow(LoaderType.FILE_PROJECTION[3]));
        path = data.getString(data.getColumnIndexOrThrow(LoaderType.FILE_PROJECTION[4]));
        type = data.getString(data.getColumnIndexOrThrow(LoaderType.FILE_PROJECTION[5]));
        size = data.getLong(data.getColumnIndexOrThrow(LoaderType.FILE_PROJECTION[6]));
        bucketId = data.getString(data.getColumnIndexOrThrow(LoaderType.FILE_PROJECTION[7]));
        bucketName = data.getString(data.getColumnIndexOrThrow(LoaderType.FILE_PROJECTION[8]));
    }

    @NonNull
    @Override
    public String toString() {
        return "MediaEntity{" + id + "," + width + "*" + height + "," + duration + "," + path + "," + type + "," + size + "," + bucketId + "," + bucketName + "}";
    }
}
