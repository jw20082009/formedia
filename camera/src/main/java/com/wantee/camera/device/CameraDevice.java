package com.wantee.camera.device;

import android.view.Surface;

import com.wantee.camera.EquipmentEnum;
import com.wantee.camera.abs.ICamera;
import com.wantee.camera.abs.ICameraChooser;
import com.wantee.camera.abs.IPreviewSizeChooser;
import com.wantee.camera.api2.CameraChooser;
import com.wantee.camera.api2.CameraOperator;
import com.wantee.camera.api2.PreviewSizeChooser;
import com.wantee.camera.api2.RequestParam;

import java.util.List;

public class CameraDevice implements IDevice{

    private String mDeviceId;
    private EquipmentEnum mEquipmentEnum = EquipmentEnum.Front_Camera2;
    private ICamera mCamera;
    private IPreviewSizeChooser mSizeChooser;
    public CameraDevice(EquipmentEnum equipmentEnum) {
        mEquipmentEnum = equipmentEnum;
    }

    @Override
    public void open(Surface surface, String captureReques) {
        if (mEquipmentEnum.isCamera2()) {
            ICameraChooser chooser = new CameraChooser();
            mCamera = new CameraOperator(chooser.onSelectIndex(mEquipmentEnum));
        }
    }

    @Override
    public void close() {
        if (mCamera != null) {
            mCamera.close();
        }
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
