package com.wantee.codec;

import android.media.MediaFormat;

public interface ICodec {

    int prepare(MediaFormat format);
}
