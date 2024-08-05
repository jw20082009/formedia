package com.wantee.camera.device;

import java.util.List;

public interface IDevice {
    void open(int template, List<RequestParam.BaseParam<?>> defaultParam);
    void close();
    void setCaptureRequest(RequestParam param);
    void setDeviceListener(IListener listener);
    IParameter parameters();
}
