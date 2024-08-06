package com.wantee.camera.device;

import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.wantee.camera.EquipmentEnum;
import com.wantee.camera.abs.ICamera;
import com.wantee.common.Constant;
import com.wantee.common.log.Log;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CameraHandler extends BaseHandler implements ICamera {
    private final String TAG = "CameraHandler";

    @Override
    public int open(EquipmentEnum type, Surface surface, String captureRequest) {
        startHandler();
        Bundle data = new Bundle();
        data.putInt("EquipmentEnum", type.ordinal());
        data.putString("Request", captureRequest);
        data.putParcelable("Surface", surface);
        boolean sendRes = sendMessage(OPEN, data, RELEASE);
        if (!sendRes) {
            Log.e(TAG, "OPEN message send failed");
        }
        return sendRes ? Constant.True: Constant.False;
    }

    @Override
    public int close() {
        int res = sendEmptyMessage(CLOSE)? Constant.True: Constant.False;
        sendEmptyMessageDelayed(RELEASE, 10_000);
        return res;
    }

    private static final int OPEN = 0x01;
    private static final int CLOSE = 0x02;
    private static final int RELEASE = 0x03;
    @Override
    void handleMessage(@NonNull Message msg) {
        switch (msg.what) {
            case OPEN:
                handleOpenCamera(msg);
                break;
            case CLOSE:
                handleCloseCamera();
                break;
            case RELEASE:
                stopHandler();
                break;
        }
    }

    private CameraDevice mDevice;
    private void handleOpenCamera(Message message) {
        Bundle data = message.getData();
        EquipmentEnum equipmentEnum = EquipmentEnum.getEquipmentEnum(data.getInt("EquipmentEnum"));
        String captureRequest = data.getString("Request");
        Surface surface = data.getParcelable("Surface");
        List<RequestParam.BaseParam<?>> params = new LinkedList<>();
        int templateType = -1;
        try {
            templateType = RequestParam.parseParam(captureRequest, params);
        } catch (JSONException | NoSuchFieldException | IllegalAccessException e) {
            Log.e(TAG, android.util.Log.getStackTraceString(e));
        }
        if (mDevice == null) {
            mDevice = new CameraDevice(equipmentEnum);
        } else if(mDevice.getEquipmentEnum() != equipmentEnum) {
            mDevice.close();
            mDevice = new CameraDevice(equipmentEnum);
        }
        mDevice.open(templateType, surface, params);
    }

    private void handleCloseCamera() {
        if (mDevice != null) {
            mDevice.close();
            mDevice = null;
        }
    }
}
