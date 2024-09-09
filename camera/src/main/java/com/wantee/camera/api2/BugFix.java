package com.wantee.camera.api2;

import android.hardware.camera2.TotalCaptureResult;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;

/**
 * Camera2在部分机型上会出现内存释放不及时问题，未释放内存主要是由native方法allocate_camera_metadata所分配，
 * 追溯堆栈到java层应该为CameraMetaDataNative.set方法而CameraMetaDataNative为CaptureResult的成员变量，
 * 通过跟踪源码以及打印onCaptureCompleted方法所返回的TotalCaptureResult可知该对象每帧都不一样，但是由于该java占用内存对象较小，
 * 所以较长时间都不会触发gc，而由于是在finalize()方法中释放native资源所以导致了native资源的堆积。
 *
 * 详情请查阅文档
 * https://blog.csdn.net/jw20082009jw/article/details/140010372?spm=1001.2014.3001.5502
 */
public class BugFix {
    static Field sTargetField;
    static Method sTargetMethod;

    private static void recycle(TotalCaptureResult tcr) {
        try {
            if (sTargetField == null) {
                Class<?> clazz = tcr.getClass().getSuperclass();
                if (clazz != null) {
                    sTargetField = clazz.getDeclaredField("mResults");
                    sTargetField.setAccessible(true);
                }
            }
            if (sTargetMethod == null) {
                Class<?> clazz = Class.forName("android.hardware.camera2.impl.CameraMetadataNative");
                Method[] methodList = clazz.getDeclaredMethods();
                Method close = null;
                Method finalize = null;
                for(Method m: methodList) {
                    if (m.getName().contains("close")) {
                        close = m;
                        break;
                    } else if (m.getName().contains("finalize")) {
                        finalize = m;
                    }
                }
                sTargetMethod = close == null? finalize: close;
                if (sTargetMethod == null) {
                    return;
                }
                sTargetMethod.setAccessible(true);
            }
            sTargetMethod.invoke(sTargetField.get(tcr));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int mMaxSize = 1;
    public BugFix(int maxSize) {
        mMaxSize = maxSize;
    }
    private final LinkedList<TotalCaptureResult> mCaptureList = new LinkedList<>();
    public void onCaptureCompleted(TotalCaptureResult result) {
        if (mMaxSize <= 0) {
            return;
        }
        if (mCaptureList.size() >= mMaxSize) {
            recycle(mCaptureList.removeFirst());
        }
        mCaptureList.offer(result);
    }

    public void clear() {
        for(TotalCaptureResult result: mCaptureList) {
            recycle(result);
        }
        mCaptureList.clear();
    }
}
