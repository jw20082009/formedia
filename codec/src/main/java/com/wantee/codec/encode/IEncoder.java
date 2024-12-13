package com.wantee.codec.encode;

import android.media.MediaFormat;
import android.view.Surface;

public interface IEncoder {
    Surface prepare(MediaFormat format);
    void onFrameAvailable(long time);
    void release();
}
