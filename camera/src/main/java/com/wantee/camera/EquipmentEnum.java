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

    public static EquipmentEnum getEquipmentEnum(int index) {
        EquipmentEnum[] enums = EquipmentEnum.values();
        for(EquipmentEnum e: enums) {
            if (e.ordinal() == index) {
                return e;
            }
        }
        return null;
    }
}
