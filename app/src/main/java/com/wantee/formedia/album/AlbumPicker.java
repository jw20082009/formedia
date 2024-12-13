package com.wantee.formedia.album;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

public class AlbumPicker {

    public enum ErrorCode{
        None, InvalidInputArgs, WithoutPermission,
    }

    public interface Callback{
        void onLoadMedia(MediaEntity data);
    }


}
