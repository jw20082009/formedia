package com.wantee.camera.device;

public interface Runtime {
    enum RuntimeType {
        Service, Handler;
    }
    RuntimeType runtimeType();
}
