package com.wantee.camera.device;

import android.view.Surface;

import com.wantee.camera.EquipmentEnum;

import java.util.List;

public interface IDevice {
    void open(int template, Surface surface, List<RequestParam.BaseParam<?>> defaultParam);
    void close();
    void setCaptureRequest(RequestParam param);
    void setDeviceListener(IListener listener);
    EquipmentEnum getEquipmentEnum();
    IParameter parameters();
}
