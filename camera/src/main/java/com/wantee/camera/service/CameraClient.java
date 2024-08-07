package com.wantee.camera.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.Surface;

import com.wantee.camera.EquipmentEnum;
import com.wantee.camera.abs.ICamera;
import com.wantee.common.Constant;
import com.wantee.common.log.Log;

import java.util.concurrent.Semaphore;

public class CameraClient implements ICamera {
    private final String TAG = "CameraClient";
    private ICameraService mCameraService;
    private final Semaphore mPermit = new Semaphore(0);
    public void startService(Context context) {
        Intent intent = new Intent("sg.bigo.cameraservice.ICamera");
        intent.setPackage(context.getPackageName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        boolean result = context.bindService(intent, mCameraConnection, Context.BIND_AUTO_CREATE);
        Log.e(TAG, "HEService startService:" + result);
    }

    final ServiceConnection mCameraConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mCameraService = ICameraService.Stub.asInterface(iBinder);
            mPermit.release();
            Log.e(TAG, "CameraClient onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mCameraService = null;
            Log.e(TAG, "CameraClient onServiceDisconnected");
        }
    };

    @Override
    public int open(EquipmentEnum type, Surface surface, String captureRequest) throws RemoteException {
        if (mCameraService != null) {
            return mCameraSevice.open(type.ordinal(), surface, captureRequest);
        }
        return Constant.Error;
    }

    @Override
    public int close() throws RemoteException {
        if (mCameraService != null) {
            return mCameraService.close();
        }
        return Constant.Error;
    }
}
