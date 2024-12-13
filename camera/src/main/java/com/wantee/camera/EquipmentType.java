package com.wantee.camera;

import android.graphics.SurfaceTexture;
import android.media.ImageReader;
import android.view.Surface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum EquipmentType {
    Front(CameraOperator.RuntimeType.Handler, false, true, SurfaceTexture.class, byte[].class),
    Rear(CameraOperator.RuntimeType.Handler, false, false, SurfaceTexture.class, byte[].class),
    Front_Camera2(CameraOperator.RuntimeType.Handler, true, true, SurfaceTexture.class, ImageReader.class),
    Rear_Camera2(CameraOperator.RuntimeType.Handler, true, false, SurfaceTexture.class, ImageReader.class);

    final CameraOperator.RuntimeType mType;
    final boolean mIsCamera2;
    final boolean mIsFront;
    final List<Class<?>> mSupportedDestination = new ArrayList<>();

    EquipmentType(CameraOperator.RuntimeType type, boolean isCamera2, boolean isFront, Class<?>... destinationClass) {
        mType = type;
        mIsCamera2 = isCamera2;
        mIsFront = isFront;
        if (destinationClass != null) {
            mSupportedDestination.addAll(Arrays.asList(destinationClass));
        }
    }

    public boolean isCamera2() {
        return mIsCamera2;
    }

    public boolean isFront() { return mIsFront; }

    public boolean isSupportedDestination(Class<?> clazz) {
        return mSupportedDestination.contains(clazz);
    }

    public CameraOperator.RuntimeType runtimeType() { return mType; }
    public static EquipmentType getEquipmentEnum(int index) {
        EquipmentType[] enums = EquipmentType.values();
        for(EquipmentType e: enums) {
            if (e.ordinal() == index) {
                return e;
            }
        }
        return null;
    }
}


