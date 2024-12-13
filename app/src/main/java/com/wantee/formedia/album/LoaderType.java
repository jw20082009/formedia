package com.wantee.formedia.album;

import android.content.Context;
import android.provider.MediaStore;

import androidx.loader.content.CursorLoader;

public enum LoaderType {
    Image(MediaStore.Files.FileColumns.MEDIA_TYPE + "=?" + " AND " + MediaStore.MediaColumns.SIZE + ">0", MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
    Video(MediaStore.Files.FileColumns.MEDIA_TYPE + "=?" + " AND " + MediaStore.MediaColumns.SIZE + ">0", MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
    ImageAndVideo( "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=? or " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)" + " AND " + MediaStore.MediaColumns.SIZE + ">0", MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE, MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO);
    private String mSelection;
    private final String[] mSelectionArgs;

    /**
     * 媒体文件数据库字段
     */
    public static final String[] FILE_PROJECTION = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            MediaStore.Video.Media.DURATION,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.SIZE,
            "bucket_id",
            "bucket_display_name",
    };

    LoaderType(String selection, int... mediaType) {
        this.mSelection = selection;
        this.mSelectionArgs = new String[mediaType.length];
        int index = 0;
        for(int type: mediaType) {
            this.mSelectionArgs[index++] = String.valueOf(type);
        }
    }

    public String getSelection() {
        return mSelection;
    }

    public String[] getSelectionArgs() {
        return mSelectionArgs;
    }

    public LoaderType excludeGif() {
        mSelection += " AND " + MediaStore.MediaColumns.MIME_TYPE + "!='image/gif'";
        return this;
    }

    public LoaderType filterDuration(long minDuration, long maxDuration) {
        mSelection += " AND " + minDuration + " <= duration and duration <= " + maxDuration;
        return this;
    }

    public CursorLoader build(Context context) {
        CursorLoader cursorLoader = new CursorLoader(context);
        cursorLoader.setUri(MediaStore.Files.getContentUri("external"));
        cursorLoader.setProjection(FILE_PROJECTION);
        cursorLoader.setSortOrder(MediaStore.Images.Media.DATE_TAKEN + " DESC");
        cursorLoader.setSelection(mSelection);
        cursorLoader.setSelectionArgs(mSelectionArgs);
        return cursorLoader;
    }
}
