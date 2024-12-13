package com.wantee.codec;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.wantee.codec.player.VideoPlayer;
import com.wantee.common.handler.BaseHandler;

import java.lang.ref.WeakReference;

public class CodecWrapper extends BaseHandler {
    private final String TAG = "CodecWrapper";

    public void handleMessage(@NonNull Message msg) {

    }

    @Override
    protected Handler createHandler(Looper looper) {
        return new CodecHandler(this, looper);
    }

    static class CodecHandler extends Handler {
        final WeakReference<CodecWrapper> mReference;
        public CodecHandler(CodecWrapper handler, Looper looper) {
            super(looper);
            mReference = new WeakReference<>(handler);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            CodecWrapper handler = mReference.get();
            if (handler != null) {
                handler.handleMessage(msg);
            }
        }
    }
}
