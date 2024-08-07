package com.wantee.camera.api2;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.os.Build;
import android.os.RemoteException;
import android.view.Surface;

import com.wantee.camera.CameraContext;
import com.wantee.camera.EquipmentEnum;
import com.wantee.camera.abs.ICamera;
import com.wantee.camera.device.Status;
import com.wantee.common.Constant;
import com.wantee.common.log.Log;

import org.json.JSONException;

import java.util.LinkedList;
import java.util.List;

public class CameraOperator implements ICamera {
    private final String TAG = "CameraOperator";
    private CameraCharacteristics mDevCharacteristics;
    private CameraManager mManager;
    private String mCameraIndex;
    private Status mStatus;

    public CameraOperator(String cameraIndex) {
        mCameraIndex = cameraIndex;
    }

    private CameraCharacteristics getCharacteristics() throws CameraAccessException {
        if (mDevCharacteristics == null) {
            mDevCharacteristics = getCameraManager(CameraContext.sContext).getCameraCharacteristics(mCameraIndex);
        }
        return mDevCharacteristics;
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

    @Override
    public int open(EquipmentEnum type, Surface surface, String captureRequest) {
        mStatus = Status.Opening;
        List<RequestParam.BaseParam<?>> params = new LinkedList<>();
        int templateType = -1;
        try {
            templateType = RequestParam.parseParam(captureRequest, params);
        } catch (JSONException | NoSuchFieldException | IllegalAccessException e) {
            Log.e(TAG, android.util.Log.getStackTraceString(e));
        }
        return Constant.True;
    }

    @Override
    public int close() {
        return 0;
    }
}
