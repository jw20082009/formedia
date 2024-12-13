package com.wantee.camera.request;

import android.hardware.camera2.CameraAccessException;
import android.view.Surface;

import com.wantee.camera.device.CameraChooser;
import com.wantee.camera.device.Status;
import com.wantee.common.Constant;
import com.wantee.common.log.Log;


public abstract class BaseCamera {
    private static final String TAG = "BaseCamera";
    private Status mStatus = Status.Unknown;
    private OpenInfo mOpenInfo;
    private CloseInfo mCloseInfo;

    public int openDevice(OpenInfo info) {
        Log.e("BaseCamera", "openDevice");
        if (isOpening() || isClosing()) {
            Log.e(TAG, "open error while " + (isOpening()? "isOpening" : "isClosing"));
            return Constant.Error;
        }
        if (mOpenInfo != null && mOpenInfo.equals(info) && isOpened()) {
            String message = info.deviceType.name() + " already opened";
            info.notifyWithoutHandle(message);
            return Constant.False;
        } else if (mOpenInfo != null) {
            Log.e(TAG, "close:" + mOpenInfo.deviceType.name() +" before open:" + info.deviceType.name());
            closeDevice();
        }
        mOpenInfo = info;
        CameraChooser cameraChooser = createCameraChooser();
        String deviceId = cameraChooser.onSelectIndex(info.deviceType);
        try {
            return openDevice(deviceId, info.preferWidth, info.preferHeight, info.captureRequest);
        } catch (Exception e) {
            info.notifyError(android.util.Log.getStackTraceString(e));
            return Constant.Error;
        }
    }

    public int closeDevice(CloseInfo info) {
        if (isOpening() || isClosing()) {
            Log.e(TAG, "close error while " + (isOpening()? "isOpening" : "isClosing"));
            return Constant.Error;
        }
        if (isClosed()) {
            info.notifyWithoutHandle("[BaseCamera] already closed");
            return Constant.False;
        }
        mCloseInfo = info;
        return closeDevice();
    }

    protected Class<?> getDestinationClass() {
        if (mOpenInfo != null) {
            return mOpenInfo.getDestinationType();
        }
        Log.e(TAG, "notifyOpen while mOpenInfo == null");
        return null;
    }

    protected Surface createDestinationSurface(int width, int height) {
        if (mOpenInfo != null) {
            return mOpenInfo.createDestinationSurface(width, height);
        }
        Log.e(TAG, "notifyOpen while mOpenInfo == null");
        return null;
    }

    protected void onDeviceOpened(int cameraDegree, int displayRotate, boolean isFacingFront) {
        if (mOpenInfo != null) {
            mOpenInfo.onOpened(cameraDegree, displayRotate, isFacingFront);
        } else {
            Log.e(TAG, "onStartPreview while mOpenInfo == null");
        }
        setStatus(Status.Opened);
    }

    protected void onDeviceClosed() {
        if (mCloseInfo != null) {
            mCloseInfo.onClose();
            mCloseInfo = null;
        } else {
            Log.e(TAG, "onClosed while mCloseInfo == null");
        }
        setStatus(Status.Closed);
    }

    protected void onCameraError(String message) {
        if (mOpenInfo != null) {
            mOpenInfo.notifyError(message);
        }
    }

    protected abstract int openDevice(String deviceId, int width, int height, String captureRequest) throws CameraAccessException;
    protected abstract int closeDevice();
    protected abstract CameraChooser createCameraChooser();
    public void setStatus(Status status) {
        mStatus = status;
    }

    boolean isOpening() {
        return mStatus == Status.Opening;
    }

    boolean isClosing() {
        return mStatus == Status.Closing;
    }

    boolean isOpened() {
        return mStatus == Status.Opened;
    }

    boolean isClosed() {
        return mStatus == Status.Closed;
    }
}
