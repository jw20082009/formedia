package com.wantee.common.handler;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CommonHandler extends BaseHandler implements BaseHandler.IHandler {

    private CommonHandler() {}

    private static volatile CommonHandler sInstance;
    public static CommonHandler instance() {
        if (sInstance == null) {
            synchronized (CommonHandler.class) {
                if (sInstance == null) {
                    sInstance = new CommonHandler();
                }
            }
        }
        return sInstance;
    }

    private final HashMap<Integer, IHandler> mHandlers = new HashMap<>();

    @Override
    protected Handler createHandler(Looper looper) {
        return new MyHandler(this, looper);
    }

    public boolean registerHandler(int what, IHandler handler) {
        synchronized (mHandlers) {
            if (mHandlers.containsKey(what)) {
                return false;
            } else {
                mHandlers.put(what, handler);
                return true;
            }
        }
    }

    public List<Integer> registerHandler(List<Integer> whatList, IHandler handler) {
        List<Integer> registerFailedList = null;
        if (whatList == null || whatList.isEmpty() || handler == null) {
            return registerFailedList;
        }
        for(Integer what: whatList) {
            if (!registerHandler(what, handler)) {
                if (registerFailedList == null) {
                    registerFailedList = new ArrayList<>();
                }
                registerFailedList.add(what);
            }
        }
        return registerFailedList;
    }

    public void removeHandler(IHandler handler) {
        synchronized (mHandlers) {
            Iterator<Map.Entry<Integer, IHandler>> it = mHandlers.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Integer, IHandler> entry = it.next();
                if (entry.getValue() == handler) {
                    it.remove();
                }
            }
        }
    }

    @Override
    public void handleMessage(@NonNull Message msg) {}

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
