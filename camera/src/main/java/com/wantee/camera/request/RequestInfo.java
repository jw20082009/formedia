package com.wantee.camera.request;

import android.view.Surface;

import androidx.annotation.NonNull;

import com.wantee.camera.preview.CameraListener;
import com.wantee.camera.EquipmentType;
import com.wantee.common.Constant;
import com.wantee.common.log.Log;

public class RequestInfo extends RequestQueue.Data {
    public enum Type {
        Unknown, Open, Close, Apply, Query
    }

    public CameraListener<?> listener;
    public long executeTime;
    public RequestInfo(RequestQueue.CheckType type, int dataTypeId, CameraListener<?> listener) {
        super(type, dataTypeId);
        this.listener = listener;
    }

    public void setListener(CameraListener<?> listener) {
        this.listener = listener;
    }

    public Type type() { return Type.Unknown; }

    public int handleRequest(BaseCamera camera) {
        return Constant.False;
    }

    public void notifyWithoutHandle(String message) {
        notifyError("WithoutHandle:" + type().name() + ", " + message);
    }

    public void notifyError(String message) {
        if (listener != null) {
            listener.onError(requestId, message);
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "RequestInfo[" + type().name()+"],id:" + requestId;
    }
}

class CloseInfo extends RequestInfo {

    public CloseInfo(CameraListener<?> listener) {
        super(RequestQueue.CheckType.Optional, Type.Close.ordinal(), listener);
    }

    @Override
    public Type type() {
        return Type.Close;
    }

    @Override
    public int handleRequest(BaseCamera camera) {
        return camera.closeDevice(this);
    }

    public void onClose() {
        if (listener != null) {
            listener.onClosed(requestId);
        }
    }
}

class OpenInfo extends RequestInfo {
    public int preferWidth;
    public int preferHeight;
    public String captureRequest;
    public EquipmentType deviceType;

    public OpenInfo(CameraListener<?> listener, EquipmentType type, int width, int height, String captureRequest) {
        super(RequestQueue.CheckType.Optional, Type.Open.ordinal(), listener);
        this.deviceType = type;
        this.preferWidth = width;
        this.preferHeight = height;
        this.captureRequest = captureRequest;
    }

    public Type type() { return Type.Open; }

    @Override
    public int handleRequest(BaseCamera camera) {
        Log.e("BaseCamera", "handleRequest");
        return camera.openDevice(this);
    }

    public Class<?> getDestinationType() {
        if (listener != null) {
            return listener.getDestinationClass();
        }
        return null;
    }

    public Surface createDestinationSurface(int width, int height) {
        if (listener != null) {
            return listener.onCreateSurface(width, height);
        }
        return null;
    }

    public Object createDestination(int width, int height) {
        if (listener != null) {
            return listener.onCreateDestination(width, height);
        }
        return null;
    }

    public void onOpened(int cameraDegree, int displayRotate, boolean isFacingFront) {
        if (listener != null) {
            listener.onOpened(requestId, cameraDegree, displayRotate, isFacingFront);
        }
    }
}






