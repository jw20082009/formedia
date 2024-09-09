package com.wantee.camera.utils;

import android.content.Context;
import android.view.Surface;
import android.view.WindowManager;

import com.wantee.camera.CameraContext;

public class CameraUtils {

    public static int getDisplayRotate() {
        Context context = CameraContext.Instance.getContext();
        int m_Orientation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (m_Orientation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        return degrees;
    }
}
