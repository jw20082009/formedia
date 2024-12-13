package com.wantee.codec.decode;

import android.media.MediaFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.wantee.common.handler.BaseHandler;
import com.wantee.common.log.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

public class DecoderWrapper extends BaseHandler {
    private final String TAG = "DecoderWrapper";
    static class DecodeParam {
        MediaFormat format;
        Surface surface;
        public DecodeParam(MediaFormat format, Surface surface) {
            this.format = format;
            this.surface = surface;
        }
        @NonNull
        @Override
        public String toString() {
            return "DecodeInfo[" + format + "," + surface+"]";
        }
    }

    private Decoder mDecoder;
    private DecodeParam mDecodeInfo;
    private final Object mLock = new Object();

    public void prepare(MediaFormat format, Surface surface) {
        synchronized (this) {
            if (mDecodeInfo != null) {
                Log.e(TAG, "prepare format:" + format + ", surface:" + surface + ", dropOld:" + mDecodeInfo);
            }
            mDecodeInfo = new DecodeParam(format, surface);
            sendMessage(MSG_PREPARE, -1, -1, format);
        }
    }

    public DecodeInfo getInputBuffer() throws InterruptedException {
        synchronized (mLock) {
            if (mDecoder != null) {
                return mDecoder.pollInputBuffer();
            }
            return null;
        }
    }

    public void offerExtractData(Extractor.ExtractData data) throws InterruptedException {
        mExtractPool.put(data);
    }

    private final int MSG_PREPARE = 0x01;
    private final int MSG_DRAIN = 0x02;

    public void handleMessage(@NonNull Message msg) {
        switch (msg.what) {
            case MSG_PREPARE:
                if (mDecodeInfo != null) {
                    try {
                        mDecoder = new Decoder();
                        mDecoder.start(mDecodeInfo.format, mDecodeInfo.surface);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                break;
            case MSG_DRAIN:
            {
                try {
                    Extractor.ExtractData data = mExtractPool.take();

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
                break;
        }
    }

    @Override
    protected Handler createHandler(Looper looper) {
        return new CodecHandler(this, looper);
    }

    static class CodecHandler extends Handler {
        final WeakReference<DecoderWrapper> mReference;
        public CodecHandler(DecoderWrapper handler, Looper looper) {
            super(looper);
            mReference = new WeakReference<>(handler);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            DecoderWrapper handler = mReference.get();
            if (handler != null) {
                handler.handleMessage(msg);
            }
        }
    }
}
