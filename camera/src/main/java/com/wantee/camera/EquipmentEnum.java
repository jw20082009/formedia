package com.wantee.camera;

public enum EquipmentEnum {
    Front(false),
    Rear(false),
    Front_Camera2(true),
    Rear_Camera2(true);

    boolean mIsCamera2 = false;

    EquipmentEnum(boolean camera2) {
        mIsCamera2 = camera2;
    }

    public boolean isCamera2() {
        return mIsCamera2;
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
