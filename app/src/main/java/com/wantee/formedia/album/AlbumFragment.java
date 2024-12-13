package com.wantee.formedia.album;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import com.wantee.common.log.Log;
import com.wantee.formedia.ui.PermissionFragment;

import java.util.Map;

public class AlbumFragment extends PermissionFragment {

    private final String TAG = "AlbumFragment";

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requestPermission();
    }

    @Override
    public String[] obtainRequiredPermissions() {
        String[] permissions = null;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            permissions = new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE };
        }
        return permissions;
    }

    @Override
    public void onPermissionResult(Map<String, Boolean> result) {
        LoaderManager.getInstance(this).initLoader(0, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @NonNull
            @Override
            public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
                return LoaderType.ImageAndVideo.excludeGif().filterDuration(0, 60000).build(getContext());
            }

            @Override
            public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
                if (data == null || data.isClosed()) return;
                while (data.moveToNext()) {
                    MediaEntity entity = new MediaEntity();
                    entity.read(data);
                    Log.e(TAG, entity.toString());
                }
                data.close();
            }

            @Override
            public void onLoaderReset(@NonNull Loader<Cursor> loader) {
            }
        });
    }

    public void pickMedia(Fragment fragment, LoaderType loader) {
        if (fragment == null || fragment.getActivity() == null) {
            return;
        }
        requestPermission();
    }
}
