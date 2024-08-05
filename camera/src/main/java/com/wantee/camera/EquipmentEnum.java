package com.wantee.camera;

public enum EquipmentEnum {
    Front(false),
    Rear(false),
    Front_WideAngle(true),
    Rear_WideAngle(true);

    boolean mIsWideAngle = false;

    EquipmentEnum(boolean wideAngle) {
        mIsWideAngle = wideAngle;
    }
}
