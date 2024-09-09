package com.wantee.camera.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.Surface;

import com.wantee.camera.EquipmentType;
import com.wantee.camera.CameraOperator;
import com.wantee.camera.PreviewListener;
import com.wantee.camera.Previewer;
import com.wantee.common.Constant;
import com.wantee.common.log.Log;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class CameraClient implements CameraOperator {
    private final String TAG = "CameraClient";
    private ICameraService mCameraService;
    private PreviewListener mMainProcessListener;
    private final Semaphore mPermit = new Semaphore(0);
    public void startService(Context context) {
        Intent intent = new Intent("com.wantee.camera.cameraservice");
        intent.setPackage(context.getPackageName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        boolean result = context.bindService(intent, mCameraConnection, Context.BIND_AUTO_CREATE);
        Log.e(TAG, "startService:" + result);
    }

    final ServiceConnection mCameraConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mCameraService = ICameraService.Stub.asInterface(iBinder);
            mPermit.release();
            Log.e(TAG, "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mCameraService = null;
            Log.e(TAG, "onServiceDisconnected");
        }
    };

    @Override
    public int open(EquipmentType deviceType, int width, int height, String captureRequest, PreviewListener listener) {
        try {
            mMainProcessListener = listener;
            boolean result = mPermit.tryAcquire(1000, TimeUnit.MILLISECONDS);
            if (!result) {
                Log.e(TAG, "OpenCamera service not started, deviceType:" + deviceType.name());
            }
            if (mCameraService != null) {
                return mCameraService.open(deviceType.ordinal(), width, height, captureRequest, mCameraListener);
            }
        } catch (RemoteException | InterruptedException ignored) {}
        return Constant.Error;
    }

    public int close() {
        try {
            if (mCameraService != null) {
                return mCameraService.close();
            }
        } catch (RemoteException ignored) {}
        return Constant.Error;
    }

    @Override
    public boolean isOpened() {
        try {
            if (mCameraService != null) {
                return mCameraService.isOpened();
            }
        } catch (RemoteException ignored) {}
        return false;
    }

    @Override
    public RuntimeType runtimeType() {
        return RuntimeType.Service;
    }

    ICameraListener mCameraListener = new ICameraListener.Stub() {
        @Override
        public ISurfacePreviewer onStartPreview(int requestCode, int equipmentType) throws RemoteException {
            if (mMainProcessListener != null) {
                Previewer<?> previewer = mMainProcessListener.onStartPreview(requestCode, EquipmentType.getEquipmentEnum(equipmentType));
                return new ISurfacePreviewer.Stub() {
                    @Override
                    public int type() throws RemoteException {
                        return previewer.type().ordinal();
                    }

                    @Override
                    public Surface createDestination(int previewWidth, int previewHeight) throws RemoteException {
                        Object obj = previewer.createDestination(previewWidth, previewHeight);
                        if (obj instanceof Surface) {
                            return (Surface) obj;
                        }
                        return null;
                    }
                };
            }
            return null;
        }

        @Override
        public void onOpened(int requestCode, int displayRotate) throws RemoteException {
            if (mMainProcessListener != null) {
                mMainProcessListener.onOpened(requestCode, displayRotate);
            }
        }

        @Override
        public void onClosed(int requestCode) throws RemoteException {
            if (mMainProcessListener != null) {
                mMainProcessListener.onClosed(requestCode);
            }
        }

        @Override
        public void onError(int requestCode, String errorMessage) throws RemoteException {
            if (mMainProcessListener != null) {
                mMainProcessListener.onError(requestCode, errorMessage);
            }
        }
    };
}
