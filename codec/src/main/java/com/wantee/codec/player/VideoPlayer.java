package com.wantee.codec.player;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.wantee.codec.decode.Extractor;
import com.wantee.common.handler.BaseHandler;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class VideoPlayer extends BaseHandler{
    private String mFilePath;
    private Extractor mVideoExtractor;

    public void setDataSource(String filepath) {
        mFilePath = filepath;

    }

    private final int MSG_PREPARE_EXTRACTOR = 0x01;
    public void handleMessage(@NonNull Message msg) {
        switch (msg.what) {
            case MSG_PREPARE_EXTRACTOR:
                if (msg.obj instanceof String) {
                    try {
                        mVideoExtractor = new Extractor();
                        mVideoExtractor.prepare((String) msg.obj, Extractor.Type.VIDEO);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                break;
        }
    }

    @Override
    protected Handler createHandler(Looper looper) {
        return new ExtractorHandler(this, looper);
    }

    static class ExtractorHandler extends Handler {
        final WeakReference<VideoPlayer> mReference;
        public ExtractorHandler(VideoPlayer handler, Looper looper) {
            super(looper);
            mReference = new WeakReference<>(handler);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            VideoPlayer handler = mReference.get();
            if (handler != null) {
                handler.handleMessage(msg);
            }
        }
    }
}
