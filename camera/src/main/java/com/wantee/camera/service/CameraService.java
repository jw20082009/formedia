package com.wantee.camera.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.Surface;

import androidx.annotation.Nullable;

public class CameraService extends Service {

    private IBinder mStub = new ICameraService.Stub() {
        @Override
        public int open(int type, Surface surface, String captureRequest) throws RemoteException {
            return 0;
        }

        @Override
        public int close() throws RemoteException {
            return 0;
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mStub;
    }
}
