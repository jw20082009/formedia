package com.wantee.camera.api2;

import static java.lang.Math.atan;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.util.SizeF;

import com.wantee.camera.EquipmentType;
import com.wantee.camera.device.CameraChooser;
import com.wantee.common.log.Log;

public class Camera2Chooser implements CameraChooser {
    private static final String TAG = "Camera2Chooser";
    private final CameraManager mManager;
    public Camera2Chooser(CameraManager manager) {
        mManager = manager;
    }

    private float getDeviceFOV(CameraCharacteristics characteristics) {
        if (null == characteristics) {
            return 0;
        }
        try {
            SizeF sizeF = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
            float[] focusLen = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
            if (sizeF == null || focusLen == null) {
                return 0;
            }
            return (float) (2 * atan(sizeF.getWidth() / (2 * focusLen[0])));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public String onSelectIndex(EquipmentType equipmentEnum) {
        if (mManager == null) {
            return null;
        }
        try {
            String[] cameraIdArray = mManager.getCameraIdList();
            if (cameraIdArray.length == 0) {
                return null;
            }
            float maxFov = Float.MIN_VALUE;
            String maxDevice = cameraIdArray[0];
            for (String cameraId : cameraIdArray) {
                CameraCharacteristics characteristics = mManager.getCameraCharacteristics(cameraId);
                Integer lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (lensFacing != null) {
                    if ((equipmentEnum.isFront() && lensFacing == CameraMetadata.LENS_FACING_FRONT) ||
                            (!equipmentEnum.isFront() && lensFacing == CameraMetadata.LENS_FACING_BACK)) {
                        float fov = getDeviceFOV(characteristics);
                        if (fov > maxFov) {
                            maxFov = fov;
                            maxDevice = cameraId;
                        }
                    }
                }
            }
            return maxDevice;
        } catch (Exception e) {
            Log.e(TAG, android.util.Log.getStackTraceString(e));
        }
        return null;
    }
}