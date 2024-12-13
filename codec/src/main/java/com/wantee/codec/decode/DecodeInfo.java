package com.wantee.codec.decode;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;

public class DecodeInfo {
    int index;
    ByteBuffer buffer;

    public DecodeInfo(int index, ByteBuffer buffer) {
        this.index = index;
        this.buffer = buffer;
    }

    public DecodeInfo setIndex(int index) {
        this.index = index;
        return this;
    }

    public DecodeInfo setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
        return this;
    }

    public void reset() {
        index = -1;
        buffer = null;
    }

    @NonNull
    @Override
    public String toString() {
        return "DecodeInfo[" + index + "," + buffer + "]";
    }
}
