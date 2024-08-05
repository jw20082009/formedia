package com.wantee.camera.abs;

import android.os.RemoteException;
import android.view.Surface;

import com.wantee.camera.EquipmentEnum;

public interface ICamera {
    int open(EquipmentEnum type, Surface surface, String captureRequest) throws RemoteException;
    int close() throws RemoteException;
}
