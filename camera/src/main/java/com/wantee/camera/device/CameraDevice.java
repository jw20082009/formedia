package com.wantee.camera.device;

import android.view.Surface;

import com.wantee.camera.EquipmentEnum;

import java.util.List;

public class CameraDevice implements IDevice{

    private String mDeviceId;
    private EquipmentEnum mEquipmentEnum = EquipmentEnum.Front_WideAngle;
    public CameraDevice(EquipmentEnum equipmentEnum) {
        mEquipmentEnum = equipmentEnum;
    }

    @Override
    public void open(int template, Surface surface, List<RequestParam.BaseParam<?>> defaultParam) {

    }

    @Override
    public void close() {

    }

    @Override
    public void setCaptureRequest(RequestParam param) {

    }

    @Override
    public void setDeviceListener(IListener listener) {

    }

    @Override
    public EquipmentEnum getEquipmentEnum() {
        return mEquipmentEnum;
    }

    @Override
    public IParameter parameters() {
        return null;
    }
}
