package com.wantee.camera;

import android.content.Context;

import com.wantee.camera.preview.CameraListener;
import com.wantee.camera.request.CameraHandler;
import com.wantee.common.log.Log;

public enum CameraContext {
    Instance;

    private static final String TAG = "CameraContext";
    private Context mContext;
    private CameraOperator mOperator;

    public synchronized void setContext(Context context) {
        mContext = context;
    }

    public synchronized Context getContext() {
        return mContext;
    }

    public synchronized int open(EquipmentType type, int preferWidth, int preferHeight, CameraListener<?> listener) {
        if (listener == null || !type.isSupportedDestination(listener.getDestinationClass()) || preferWidth <= 0 || preferHeight <= 0) {
            Log.e(TAG, "open camera failed, invalid arguments, EquipmentType:" + type +
                    ",preferSize:" + preferWidth + "*" + preferHeight + ",listener:" + listener +
                    ",listenerDestination:" + (listener == null? "-1": listener.getDestinationClass()));
            throw new RuntimeException("CameraContext.open invalidArguments");
        }
        if (mOperator == null) {
            mOperator = new CameraHandler();
        }
        return mOperator.open(type, preferWidth, preferHeight, "", listener);
    }

    public synchronized int setListener(CameraListener<?> listener) {
        if (mOperator == null) {
            return -1;
        }
        return mOperator.setListener(listener);
    }

    public synchronized int close() {
        if (mOperator != null) {
            return mOperator.close();
        }
        return -1;
    }
}
