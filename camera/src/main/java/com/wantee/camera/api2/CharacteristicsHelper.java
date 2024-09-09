package com.wantee.camera.api2;

import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Build;
import android.util.Range;
import android.util.Size;
import android.view.Surface;

import com.wantee.camera.PreviewType;
import com.wantee.camera.Previewer;
import com.wantee.camera.utils.CameraUtils;
import com.wantee.common.Constant;
import com.wantee.common.log.Log;

import org.json.JSONException;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class CharacteristicsHelper {
    private static final String TAG = "CharacteristicsHelper";
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private final CameraCharacteristics mDevCharacteristics;
    private final HashMap<String, RequestParam.BaseParam<?>> mParamMap = new HashMap<>();
    private final HashMap<String, RequestParam.IParamChecker<?>> mParamChecker = new HashMap<>();
    public CharacteristicsHelper(CameraCharacteristics characteristics) {
        mDevCharacteristics = characteristics;
    }

    public <T> T getCameraCharacteristics(CameraCharacteristics.Key<T> key) {
        if (mDevCharacteristics == null) {
            return null;
        }
        return mDevCharacteristics.get(key);
    }

    public Size[] getSupportedPreviewSize(Previewer<?> previewer) {
        StreamConfigurationMap map = getCameraCharacteristics(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (map == null) {
            return new Size[] {};
        }
        if (previewer.type() == PreviewType.SurfaceTexture) {
            return map.getOutputSizes(SurfaceTexture.class);
        } else {
            return map.getOutputSizes(ImageReader.class);
        }
    }

    private void setLocalCaptureConfig() {
        setRequest("NOISE_REDUCTION_MODE", CaptureRequest.NOISE_REDUCTION_MODE, CameraMetadata.NOISE_REDUCTION_MODE_FAST);
        setRequest("STATISTICS_FACE_DETECT_MODE", CaptureRequest.STATISTICS_FACE_DETECT_MODE, CameraMetadata.STATISTICS_FACE_DETECT_MODE_SIMPLE);
        setRequest("CONTROL_VIDEO_STABILIZATION_MODE", CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_OFF);
        setRequest("LENS_OPTICAL_STABILIZATION_MODE", CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_OFF);
        setRequest("CONTROL_AE_ANTIBANDING_MODE", CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, CameraCharacteristics.CONTROL_AE_ANTIBANDING_MODE_AUTO);
        setRequest("CONTROL_MODE", CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        setRequest("CONTROL_AE_MODE", CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
        setRequest("CONTROL_AF_MODE", CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO);
        setRequest("CONTROL_AWB_MODE", CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO);
    }

    public void startCaptureSession(String captureParam, CameraDevice device, Surface surface, CameraCaptureSession.StateCallback callback) throws JSONException, NoSuchFieldException, IllegalAccessException, CameraAccessException {
        List<RequestParam.BaseParam<?>> params = new LinkedList<>();
        int templateType = RequestParam.parseParam(captureParam, params);
        if (templateType == -1) {
            templateType = CameraDevice.TEMPLATE_PREVIEW;
        }
        mPreviewRequestBuilder = device.createCaptureRequest(templateType);
        setLocalCaptureConfig();// 本地配置先设置，如果线上配置有相同key，会被线上配置覆盖
        if (mPreviewRequestBuilder == null) {
            Log.e(TAG, "startCaptureSession err mPreviewRequestBuilder == null");
        }
        for (RequestParam.BaseParam<?> param: params) {
            param.setChecker(obtainChecker(param.getKeyStr()));
            param.configBuilder(mPreviewRequestBuilder);
            mParamMap.put(param.getKeyStr(), param);
        }
        mPreviewRequestBuilder.addTarget(surface);
        device.createCaptureSession(Collections.singletonList(surface), callback, null);
    }

    public CaptureRequest.Builder getRequestBuilder() {
        return mPreviewRequestBuilder;
    }

    public int getWindowDegree() {
        return CameraUtils.getDisplayRotate();
    }

    private <T> int setRequest(String keyStr, CaptureRequest.Key<T> key, T value) {
        if (mPreviewRequestBuilder == null) {
            return Constant.Error;
        }
        RequestParam.BaseParam<?> param = mParamMap.get(keyStr);
        if (param == null) {
            param = RequestParam.createParam(value.getClass(), keyStr);
        }
        if (param != null) {
            mParamMap.put(keyStr, param);
            RequestParam.BaseParam<T> param2 = (RequestParam.BaseParam<T>) param;
            param2.setParam(key, value);
            param2.setChecker(obtainChecker(keyStr));
            param2.configBuilder(mPreviewRequestBuilder);
            return Constant.True;
        }
        return Constant.False;
    }

    public int[] getSupportedFlashModes() {
        return new int[]{ CaptureRequest.FLASH_MODE_OFF, CaptureRequest.FLASH_MODE_SINGLE, CaptureRequest.FLASH_MODE_TORCH};
    }

    class CommonChecker<T> implements RequestParam.IParamChecker<T> {
        private CameraCharacteristics.Key<T[]> mKey;
        public CommonChecker(CameraCharacteristics.Key<T[]> key) {
            mKey = key;
        }

        @Override
        public boolean isValid(T data) {
            if (mDevCharacteristics == null || data == null) {
                return false;
            }
            T[] supportedList = mDevCharacteristics.get(mKey);
            if (supportedList == null) {
                return false;
            }
            for(T t: supportedList) {
                if (data.equals(t)) {
                    return true;
                }
            }
            return false;
        }
    }

    class IntChecker implements RequestParam.IParamChecker<Integer> {
        private CameraCharacteristics.Key<int[]> mKey;
        private CameraCharacteristics.Key<Range<Integer>> mRangeKey;
        private int[] mCheckData;
        private int mLimitSdkInt = 0;
        public IntChecker(CameraCharacteristics.Key<int[]> key) {
            mKey = key;
        }

        public IntChecker setCheckData(int[] data) {
            mCheckData = data;
            return this;
        }

        public IntChecker setRangeKey(CameraCharacteristics.Key<Range<Integer>> key) {
            mRangeKey = key;
            return this;
        }

        public IntChecker setLimitSdkInt(int limitApi) {
            mLimitSdkInt = limitApi;
            return this;
        }

        @Override
        public boolean isValid(Integer data) {
            if (mDevCharacteristics == null || data == null || Build.VERSION.SDK_INT < mLimitSdkInt) {
                return false;
            }
            if (mRangeKey != null) {
                Range<Integer> range = mDevCharacteristics.get(mRangeKey);
                return range != null && data >= range.getLower() && data < range.getUpper();
            }
            int[] supportedList = mCheckData;
            if (supportedList == null) {
                supportedList = mDevCharacteristics.get(mKey);
            }
            if (supportedList == null) {
                return false;
            }
            for(int t: supportedList) {
                if (data.equals(t)) {
                    return true;
                }
            }
            return false;
        }
    }

    private RequestParam.IParamChecker<?> createChecker(String key) {
        switch (key) {
            case "CONTROL_AE_TARGET_FPS_RANGE":
                return new CommonChecker<>(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
            case "STATISTICS_FACE_DETECT_MODE":
                return new IntChecker(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES);
            case "FLASH_MODE":
                return new IntChecker(null).setCheckData(getSupportedFlashModes());
            case "CONTROL_VIDEO_STABILIZATION_MODE": {
                int[] checkData = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    checkData = new int[]{CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_OFF, CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_ON, CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_PREVIEW_STABILIZATION};
                } else {
                    checkData = new int[]{CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_OFF, CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_ON};
                }
                return new IntChecker(null).setCheckData(checkData);
            }
            case "CONTROL_AE_ANTIBANDING_MODE":
                return new IntChecker(CameraCharacteristics.CONTROL_AE_AVAILABLE_ANTIBANDING_MODES);
            case "CONTROL_AF_MODE":
                return new IntChecker(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
            case "CONTROL_AE_EXPOSURE_COMPENSATION":
                return new IntChecker(null).setRangeKey(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
            case "CONTROL_AE_MODE":
                return new IntChecker(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES);
            case "CONTROL_AE_PRECAPTURE_TRIGGER":
                return new IntChecker(null).setCheckData(new int[]{CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_IDLE, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_CANCEL});
            case "CONTROL_ZOOM_RATIO":
                return (RequestParam.IParamChecker<Float>) data -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && data != null) {
                        Range<Float> range = getCameraCharacteristics(CameraCharacteristics.CONTROL_ZOOM_RATIO_RANGE);
                        return range != null && data >= range.getLower() && data <= range.getUpper();
                    }
                    return true;
                };
            case "CONTROL_MODE":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    return new IntChecker(CameraCharacteristics.CONTROL_AVAILABLE_MODES).setLimitSdkInt(Build.VERSION_CODES.M);
                }

        }
        return null;
    }

    private RequestParam.IParamChecker<?> obtainChecker(String key) {
        if (!mParamChecker.containsKey(key)) {
            RequestParam.IParamChecker<?> checker = createChecker(key);
            if (checker != null) {
                mParamChecker.put(key, checker);
                return checker;
            }
            return null;
        }
        return mParamChecker.get(key);
    }
}
