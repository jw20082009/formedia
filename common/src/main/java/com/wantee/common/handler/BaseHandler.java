package com.wantee.common.handler;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

public abstract class BaseHandler {

    public interface IHandler {
        void handleMessage(@NonNull Message msg);
    }

    private static final String TAG = "BaseHandler";
    private Handler mHandler;
    private final Object mLock = new Object();

    protected abstract Handler createHandler(Looper looper);

    public Handler handler() {
        synchronized (mLock) {
            return mHandler;
        }
    }

    public void startHandler() {
        synchronized (mLock) {
            if (mHandler == null) {
                HandlerThread thread = new HandlerThread(TAG);
                thread.start();
                mHandler = createHandler(thread.getLooper());;
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

    public boolean sendEmptyMessageAndRemove(int what, int... removeWhat) {
        synchronized (mLock) {
            if (mHandler == null) {
                return false;
            }
            for(int r: removeWhat) {
                mHandler.removeMessages(r);
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
}
