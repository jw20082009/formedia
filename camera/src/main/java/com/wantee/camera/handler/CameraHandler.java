package com.wantee.camera.handler;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.wantee.camera.EquipmentEnum;
import com.wantee.camera.abs.ICamera;
import com.wantee.common.Constant;
import com.wantee.common.log.Log;

import java.lang.ref.WeakReference;

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
                break;
            case CLOSE:
                break;
            case RELEASE:
                break;
        }
    }
}
