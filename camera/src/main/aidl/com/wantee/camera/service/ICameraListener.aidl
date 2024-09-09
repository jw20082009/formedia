// ICameraListener.aidl
package com.wantee.camera.service;

// Declare any non-default types here with import statements
import com.wantee.camera.service.ISurfacePreviewer;
interface ICameraListener {
    ISurfacePreviewer onStartPreview(int requestCode, int equipmentType);
    void onOpened(int requestCode, int displayRotate);
    void onClosed(int requestCode);
    void onError(int requestCode, String errorMessage);
}