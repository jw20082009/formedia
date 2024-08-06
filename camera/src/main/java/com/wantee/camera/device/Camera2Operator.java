package com.wantee.camera.device;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.os.Build;

import com.wantee.camera.CameraContext;
import com.wantee.common.log.Log;

public class Camera2Operator {
    private final String TAG = "CameraOperator";
    private CameraCharacteristics mDevCharacteristics;
    private CameraManager mManager;
    private String mCameraIndex;
    private Status mStatus;

    public Camera2Operator(String cameraIndex) {
        mCameraIndex = cameraIndex;
    }

    private CameraCharacteristics getCharacteristics() throws CameraAccessException {
        if (mDevCharacteristics == null) {
            mDevCharacteristics = getCameraManager(CameraContext.sContext).getCameraCharacteristics(mCameraIndex);
        }
        return mDevCharacteristics;
    }

    public void open(String cameraIndex) {
        mStatus = Status.Opening;
        mCameraIndex = cameraIndex;

    }

    private CameraManager getCameraManager(Context context) {
        if (mManager == null) {
            mManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        }
        if (mManager == null) {
            throw new RuntimeException("mManager == null");
        }
        return mManager;
    }

    public boolean isHardwareSupported() {
        try {
            CameraCharacteristics characteristics = getCharacteristics();
            Integer level = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
            if (level != null && (level == CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED
                    || level == CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_FULL
                    || level == CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_3)) {
                return true;
            }
            Log.e(TAG, "Device support for api level is LEGACY, level" + level);
            return false;
        } catch (CameraAccessException e) {
            Log.e(TAG, e.toString());
        }
        return false;
    }

    private boolean isGetCameraPermission() {
        if (CameraContext.sContext != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                CameraContext.sContext.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "have no camera permission:" + Build.VERSION.SDK_INT);
            return false;
        }
        return true;
    }
}
