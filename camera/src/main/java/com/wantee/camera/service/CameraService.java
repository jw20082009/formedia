package com.wantee.camera.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.Surface;

import androidx.annotation.Nullable;
import com.wantee.camera.EquipmentType;
import com.wantee.camera.PreviewType;
import com.wantee.camera.request.CameraHandler;

import com.wantee.camera.PreviewListener;
import com.wantee.camera.Previewer;
import com.wantee.common.Constant;

public class CameraService extends Service {
    private CameraHandler mCameraHandler;
    private ICameraListener mMainProcessListener;
    private final Object mLock = new Object();
    private void setCameraListener(ICameraListener listener) {
        synchronized (mLock) {
            mMainProcessListener = listener;
        }
    }

    private ISurfacePreviewer onMainStartPreview(int requestCode, EquipmentType equipmentType) {
        synchronized (mLock) {
            try {
                return mMainProcessListener.onStartPreview(requestCode, equipmentType.ordinal());
            } catch (RemoteException ignored) {
            }
            return null;
        }
    }

    private void onMainOpened(int requestCode, int displayRotate) {
        synchronized (mLock) {
            try {
                mMainProcessListener.onOpened(requestCode, displayRotate);
            } catch (RemoteException ignored) {
            }
        }
    }

    private void onMainClosed(int requestCode) {
        synchronized (mLock) {
            try {
                mMainProcessListener.onClosed(requestCode);
            } catch (RemoteException ignored) {
            }
        }
    }

    private void onMainError(int requestCode, String errorMessage) {
        synchronized (mLock) {
            try {
                mMainProcessListener.onError(requestCode, errorMessage);
            } catch (RemoteException ignored) {
            }
        }
    }

    private final IBinder mStub = new ICameraService.Stub() {

        @Override
        public int open(int deviceType, int width, int height, String captureRequest, ICameraListener listener) throws RemoteException {
            EquipmentType enumType = EquipmentType.getEquipmentEnum(deviceType);
            if (enumType == null) {
                return Constant.False;
            }
            if (mCameraHandler == null) {
                mCameraHandler = new CameraHandler();
            }
            return mCameraHandler.open(enumType, width, height, captureRequest, mServiceProcessListener);
        }

        @Override
        public int close() throws RemoteException {
            if (mCameraHandler == null) {
                return Constant.Error;
            }
            return mCameraHandler.close();
        }

        @Override
        public boolean isOpened() throws RemoteException {
            if (mCameraHandler == null) {
                return false;
            }
            return mCameraHandler.isOpened();
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mStub;
    }

    PreviewListener mServiceProcessListener = new PreviewListener() {

        @Override
        public Previewer<?> onStartPreview(int requestCode, EquipmentType equipmentType) {
            ISurfacePreviewer previewer = onMainStartPreview(requestCode, equipmentType);
            if (previewer == null) {
                return null;
            }
            try {
                int type = previewer.type();
                return new Previewer<Surface>() {
                    @Override
                    public PreviewType type() {
                        return PreviewType.getPreviewType(type);
                    }

                    @Override
                    public Surface createDestination(int previewWidth, int previewHeight) {
                        try {
                            return previewer.createDestination(previewWidth, previewHeight);
                        } catch (RemoteException ignored) {
                        }
                        return null;
                    }
                };
            } catch (RemoteException ignored) {
            }
            return null;
        }

        @Override
        public void onOpened(int requestCode, int displayRotate) {
            onMainOpened(requestCode, displayRotate);
        }

        @Override
        public void onClosed(int requestCode) {
            onMainClosed(requestCode);
        }

        @Override
        public void onError(int requestCode, String errorMessage) {
            onMainError(requestCode, errorMessage);
        }
    };
}
