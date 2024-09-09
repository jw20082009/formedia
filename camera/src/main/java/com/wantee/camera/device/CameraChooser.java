package com.wantee.camera.device;

import com.wantee.camera.EquipmentType;
public interface CameraChooser {
    String onSelectIndex(EquipmentType equipmentEnum);
}