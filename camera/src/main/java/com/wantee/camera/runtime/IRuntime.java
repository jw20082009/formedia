package com.wantee.camera.runtime;

import android.view.Surface;

public interface IRuntime {
    int open(int type, Surface surface, String captureRequest);
    int close();
}
