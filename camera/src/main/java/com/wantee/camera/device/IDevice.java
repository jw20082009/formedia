package com.wantee.camera.device;

import android.view.Surface;

import com.wantee.camera.EquipmentEnum;
import com.wantee.camera.api2.RequestParam;

import java.util.List;

public interface IDevice {
    void open(Surface surface, String captureRequest);
    void close();
    void setCaptureRequest(RequestParam param);
    void setDeviceListener(IListener listener);
    EquipmentEnum getEquipmentEnum();
    IParameter parameters();
}
