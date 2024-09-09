package com.wantee.camera.request;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.wantee.camera.EquipmentType;
import com.wantee.camera.CameraOperator;
import com.wantee.camera.PreviewListener;
import com.wantee.camera.api2.Camera2Impl;
import com.wantee.common.handler.BaseHandler;
import com.wantee.common.log.Log;

import java.lang.ref.WeakReference;

public class CameraHandler extends BaseHandler implements CameraOperator, BaseHandler.IHandler {
    private final String TAG = "CameraHandler";
    private final RequestQueue<RequestInfo> mRequestQueue = new RequestQueue<>(new RequestQueue.IQueueListener<RequestInfo>() {
        @Override
        public void onAutoRemove(RequestInfo data) {
            Log.e(TAG, "onAutoRemove:" + data);
            data.notifyWithoutHandle();
        }

        @Override
        public void onDrop(RequestInfo data) {
            Log.e(TAG, "onDrop:" + data);
            data.notifyWithoutHandle();
        }
    });
    private final Object mLock = new Object();
    private BaseCamera mCamera;
    private OpenInfo mOpenInfo;

    public CameraHandler() {
    }

    public int open(EquipmentType type, int width, int height, String captureRequest, PreviewListener<?> listener) {
        Log.e(TAG, "open, type:" + type + ", preferSize:" + width + "*" + height + ", captureRequest:" + captureRequest + ", listener:" + listener);
        synchronized (mLock) {
            startHandler();
            sendEmptyMessageAndRemove(DRAIN, DRAIN, RELEASE);
            return mRequestQueue.offer(new OpenInfo(listener, type, width, height, captureRequest));
        }
    }

    public int close() {
        Log.e(TAG, "close");
        synchronized (mLock) {
            sendEmptyMessageAndRemove(DRAIN, DRAIN, RELEASE);
            sendEmptyMessageDelayed(RELEASE, 10_000);
            CloseInfo info = new CloseInfo(mOpenInfo.listener);
            info.setOptionalChecker((RequestQueue.IOptionalChecker<CloseInfo>) data -> mOpenInfo != null);
            return mRequestQueue.offer(info);
        }
    }

    @Override
    public boolean isOpened() {
        synchronized (mLock) {
            if (mCamera != null) {
                return mCamera.isOpened();
            }
        }
        return false;
    }

    @Override
    public RuntimeType runtimeType() {
        return RuntimeType.Handler;
    }

    private static final int DRAIN = 0x01;
    private static final int RELEASE = 0x02;

    @Override
    protected Handler createHandler(Looper looper) {
        return new MyHandler(this, looper);
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        switch (msg.what) {
            case DRAIN:
                RequestInfo info = null;
                BaseCamera camera = null;
                synchronized (mLock) {
                    info = mRequestQueue.peek();
                    Log.e(TAG, "drain info:" + info);
                    if (info == null) {
                        return;
                    }
                    camera = mCamera;
                    if (mCamera != null && (mCamera.isOpening() || mCamera.isClosing())) {
                        sendEmptyMessageAndRemove(DRAIN, DRAIN);
                        return;
                    }
                    if (info.type() == RequestInfo.Type.Open && mCamera == null) {
                        mCamera = new Camera2Impl();
                        mOpenInfo = (OpenInfo) info;
                        camera = mCamera;
                    } else if (info.type() == RequestInfo.Type.Close && mCamera != null) {
                        mCamera = null;
                        mOpenInfo = null;
                    }
                    if (camera == null) {
                        Log.e(TAG, "requestType:" + info.type() + " when camera == null");
                        return;
                    }
                    mRequestQueue.remove();
                }
                info.handleRequest(camera);
                sendEmptyMessageAndRemove(DRAIN, DRAIN);
                break;
            case RELEASE:
                stopHandler();
                break;
        }
    }

    static class MyHandler extends Handler {
        final WeakReference<IHandler> mReference;
        public MyHandler(IHandler handler, Looper looper) {
            super(looper);
            mReference = new WeakReference<>(handler);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            IHandler camera = mReference.get();
            if (camera != null) {
                camera.handleMessage(msg);
            }
        }
    }
}
