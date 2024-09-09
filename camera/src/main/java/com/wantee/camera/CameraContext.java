package com.wantee.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.Surface;

import com.wantee.camera.device.Runtime;
import com.wantee.camera.request.CameraHandler;
import com.wantee.camera.service.CameraClient;
import com.wantee.common.log.Log;

import java.util.HashMap;

public enum CameraContext {
    Instance;
    private static final String TAG = "CameraContext";
    private Context mContext;
    private final HashMap<Runtime.RuntimeType, Integer> mRuntimeSwitchCode = new HashMap<>();
    private CameraOperator mOperator;

    public void setContext(Context context) {
        mContext = context;
    }

    public Context getContext() { return mContext; }

    private CameraOperator createOperator(EquipmentType type) {
        Log.e(TAG, "createOperator:" + type.name());
        if (type.runtimeType() == Runtime.RuntimeType.Service) {
            return new CameraClient();
        }
        return new CameraHandler();
    }

    public int open(EquipmentType type, int preferWidth, int preferHeight) {
        synchronized (mRuntimeSwitchCode) {
            if (mOperator == null) {
                CameraOperator operator = createOperator(type);
                int requestCode = operator.open(type, preferWidth, preferHeight, "", mListenerWrapper);
                if (requestCode >= 0) {
                    mOperator = operator;
                }
                return requestCode;
            }
            if (mOperator.runtimeType() != type.runtimeType()) {
                if (mRuntimeSwitchCode.containsKey(type.runtimeType())) {
                    Log.e(TAG, "open["+ type.name() +"] when is closing old");
                    // 旧的runtimeType正在关闭中
                    return -1;
                } else if (mOperator.isOpened()) {
                    // 切换RuntimeType需要先关闭旧的
                    int requestCode = mOperator.close();
                    mRuntimeSwitchCode.put(mOperator.runtimeType(), requestCode);
                    Log.e(TAG, "switch runtime["+type.runtimeType().name()+"] need close old["+ mOperator.runtimeType().name()+"] first");
                    return -1;
                }
                mOperator = createOperator(type);
            }
            return mOperator.open(type, preferWidth, preferHeight, "", mListenerWrapper);
        }
    }

    public void setSurfaceListener(PreviewListener<Surface> listener) {
        mListenerWrapper.setSurfaceListener(listener);
    }

    public void setSurfaceTextureListener(PreviewListener<SurfaceTexture> listener) {
        mListenerWrapper.setSurfaceTextureListener(listener);
    }

    public int close() {
        synchronized (mRuntimeSwitchCode) {
            if (mOperator != null) {
                return mOperator.close();
            }
            return -1;
        }
    }


    private final ListenerWrapper mListenerWrapper = new ListenerWrapper() {

        private void onRuntimeSwitchResult(int requestCode) {
            synchronized (mRuntimeSwitchCode) {
                if (mOperator == null) {
                    return;
                }
                CameraOperator.RuntimeType type = mOperator.runtimeType();
                Integer codeObj = mRuntimeSwitchCode.get(type);
                if (codeObj != null && codeObj == requestCode) {
                    mRuntimeSwitchCode.remove(type);
                }
            }
        }

        @Override
        public void onClosed(int requestCode) {
            super.onClosed(requestCode);
            onRuntimeSwitchResult(requestCode);
        }

        @Override
        public void onError(int requestCode, String errorMessage) {
            super.onError(requestCode, errorMessage);
            onRuntimeSwitchResult(requestCode);
            Log.e(TAG, errorMessage);
        }
    };
}
