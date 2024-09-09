// ISurfacePreviewer.aidl
package com.wantee.camera.service;

// Declare any non-default types here with import statements

interface ISurfacePreviewer {
    int type();
    Surface createDestination(int previewWidth, int previewHeight);
}