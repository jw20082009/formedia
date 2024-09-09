package com.wantee.camera;

import android.graphics.SurfaceTexture;
import android.view.Surface;

public enum EquipmentType {
    Front(CameraOperator.RuntimeType.Handler, false, true, SurfaceTexture.class),
    Rear(CameraOperator.RuntimeType.Handler, false, false, SurfaceTexture.class),
    Front_Camera2(CameraOperator.RuntimeType.Handler, true, true, Surface.class),
    Rear_Camera2(CameraOperator.RuntimeType.Handler, true, false, Surface.class),
    Front_Camera2Service(CameraOperator.RuntimeType.Service, true, true, Surface.class),
    Rear_Camera2Service(CameraOperator.RuntimeType.Service, true, false, Surface.class);

    final CameraOperator.RuntimeType mType;
    final boolean mIsCamera2;
    final boolean mIsFront;
    final Class<?> mPreviewClazz;

    EquipmentType(CameraOperator.RuntimeType type, boolean isCamera2, boolean isFront, Class<?> previewClazz) {
        mType = type;
        mIsCamera2 = isCamera2;
        mIsFront = isFront;
        mPreviewClazz = previewClazz;
    }

    public boolean isCamera2() {
        return mIsCamera2;
    }

    public boolean isFront() { return mIsFront; }
    public Class<?> previewClazz() { return mPreviewClazz; }

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


