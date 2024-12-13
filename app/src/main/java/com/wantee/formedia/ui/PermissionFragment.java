package com.wantee.formedia.ui;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.wantee.common.log.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class PermissionFragment extends Fragment {
    private final String TAG = "PermissionFragment";
    private int mRequestTimes = 0;

    ActivityResultLauncher<String[]> mPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
        @Override
        public void onActivityResult(Map<String, Boolean> o) {
            Log.e(TAG, "onActivityResult:" + o);
            onPermissionResult(o);
        }
    });

    public static String[] checkPermission(Context context, String[] permissions) {
        if (permissions == null || permissions.length == 0) {
            return null;
        }
        if (Build.VERSION.SDK_INT >= 23) {
            List<String> needGrantPermissions = new ArrayList<>();
            for (String p : permissions) {
                int granted = context.checkSelfPermission(p);
                if (granted != PackageManager.PERMISSION_GRANTED) {
                    needGrantPermissions.add(p);
                }
            }
            return needGrantPermissions.toArray(new String[]{});
        }
        return null;
    }

    public abstract String[] obtainRequiredPermissions();

    public abstract void onPermissionResult(Map<String, Boolean> result);

    public void requestPermission() {
        String[] requiredPermissions = obtainRequiredPermissions();
        String[] permissions = checkPermission(getActivity(), requiredPermissions);
        Log.e(TAG, "requestPermission:" + Arrays.toString(permissions));
        if (permissions == null || permissions.length == 0) {
            HashMap<String, Boolean> successMap = new HashMap<>();
            for (String permission: requiredPermissions) {
                successMap.put(permission, true);
            }
            onPermissionResult(successMap);
        } else {
            if (mRequestTimes <= requiredPermissions.length) {
                mPermissionLauncher.launch(requiredPermissions);
                mRequestTimes ++;
            }
        }
    }
}
