package com.wantee.camera.device;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

public abstract class BaseHandler {
    private static final String TAG = "BaseHandler";
    private MyHandler mHandler;
    private final Object mLock = new Object();

    public void startHandler() {
        synchronized (mLock) {
            if (mHandler == null) {
                HandlerThread thread = new HandlerThread(TAG);
                thread.start();
                mHandler = new MyHandler(this, thread.getLooper());
            }
        }
    }

    public void stopHandler() {
        synchronized (mLock) {
            if (mHandler != null) {
                mHandler.getLooper().quitSafely();
                mHandler = null;
            }
        }
    }

    public boolean sendMessage(Message message, int removeMessageWhat) {
        synchronized (mLock) {
            if (mHandler == null || message == null) {
                return false;
            }
            if (removeMessageWhat >= 0) {
                mHandler.removeMessages(removeMessageWhat);
            }
            return mHandler.sendMessage(message);
        }
    }

    public boolean sendEmptyMessage(int what) {
        synchronized (mLock) {
            if (mHandler == null) {
                return false;
            }
            return mHandler.sendEmptyMessage(what);
        }
    }

    public boolean sendEmptyMessageDelayed(int what, long milli) {
        synchronized (mLock) {
            if (mHandler == null) {
                return false;
            }
            return mHandler.sendEmptyMessageDelayed(what, milli);
        }
    }

    public Message obtainMessage(int what) {
        synchronized (mLock) {
            if (mHandler == null) {
                return null;
            }
            return mHandler.obtainMessage(what);
        }
    }

    public boolean sendMessage(int what, int arg1, int arg2, Object obj) {
        Message message = obtainMessage(what);
        if (message != null) {
            message.arg1 = arg1;
            message.arg2 = arg2;
            message.obj = obj;
            return sendMessage(message, -1);
        }
        return false;
    }

    public boolean sendMessage(int what, Bundle data, int removeMessageWhat) {
        Message message = obtainMessage(what);
        if (message != null) {
            message.setData(data);
            return sendMessage(message, removeMessageWhat);
        }
        return false;
    }

    static class MyHandler extends Handler {
        final WeakReference<BaseHandler> mReference;
        public MyHandler(BaseHandler handler, Looper looper) {
            super(looper);
            mReference = new WeakReference<>(handler);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            BaseHandler camera = mReference.get();
            if (camera != null) {
                camera.handleMessage(msg);
            }
        }
    }

    abstract void handleMessage(@NonNull Message msg);
}
