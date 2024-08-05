package com.wantee.camera.device;

import java.util.List;

public class CameraDevice implements IDevice{

    @Override
    public void open(int template, List<RequestParam.BaseParam<?>> defaultParam) {

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
    public IParameter parameters() {
        return null;
    }
}
