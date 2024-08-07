package com.wantee.camera.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.Surface;

import androidx.annotation.Nullable;
import com.wantee.camera.EquipmentEnum;
import com.wantee.camera.device.CameraHandler;

import com.wantee.common.Constant;

public class CameraService extends Service {
    private CameraHandler mCameraHandler;
    private IBinder mStub = new ICameraService.Stub() {
        @Override
        public int open(int type, Surface surface, String captureRequest) throws RemoteException {
            EquipmentEnum enumType = EquipmentEnum.getEquipmentEnum(type);
            if (enumType == null) {
                return Constant.False;
            }
            if (mCameraHandler == null) {
                mCameraHandler = new CameraHandler();
            }
            return mCameraHandler.open(enumType, surface, captureRequest);
        }

        @Override
        public int close() throws RemoteException {
            if (mCameraHandler == null) {
                return Constant.False;
            }
            return mCameraHandler.close();
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mStub;
    }
}
