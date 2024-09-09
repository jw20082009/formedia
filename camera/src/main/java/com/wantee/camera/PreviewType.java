package com.wantee.camera;

public enum PreviewType {
    Surface, SurfaceTexture, Yuv420;

    public static PreviewType getPreviewType(int index) {
        PreviewType[] enums = PreviewType.values();
        for(PreviewType e: enums) {
            if (e.ordinal() == index) {
                return e;
            }
        }
        return null;
    }
}
