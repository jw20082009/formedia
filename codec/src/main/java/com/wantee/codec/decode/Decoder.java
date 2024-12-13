package com.wantee.codec.decode;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.wantee.codec.VideoFile;
import com.wantee.common.log.Log;
import com.wantee.common.queue.BlockPool;
import com.wantee.common.queue.PoolListener;

import java.io.IOException;

public class Decoder extends VideoFile {

    private final BlockPool<DecodeInfo> mInputPool = new BlockPool<>(200, 10, new PoolListener<>() {
        @Override
        public DecodeInfo onCreate(int id) {
            Log.e(TAG, "mInputPool onCreate:" + id);
            return new DecodeInfo(-1, null);
        }
    });

    private final BlockPool<DecodeInfo> mOutputPool = new BlockPool<>(200, 10, new PoolListener<DecodeInfo>() {
        @Override
        public DecodeInfo onCreate(int id) {
            Log.e(TAG, "mOutputPool onCreate:" + id);
            return new DecodeInfo(-1, null);
        }
    });

    private final String TAG = "Decoder";
    private MediaCodec mDecoder;
    private boolean mIsOutputSurface = false;

    private void offerInputInfo(DecodeInfo info) throws InterruptedException {
        mInputPool.offer(info);
    }

    private void offerOutputInfo(DecodeInfo info) throws InterruptedException {
        mOutputPool.offer(info);
    }

    public DecodeInfo pollInputBuffer() throws InterruptedException {
        return mInputPool.poll();
    }

    public void releaseOutputBuffer(DecodeInfo info, boolean render) throws InterruptedException {
        if (mDecoder == null || info == null) {
            return;
        }
        mDecoder.releaseOutputBuffer(info.index, render);
        info.reset();
        mOutputPool.free(info);
    }

    public DecodeInfo pollOutputBuffer() throws InterruptedException {
        return mOutputPool.poll();
    }

    public int start(MediaFormat format, Surface surface) throws IOException {
        Log.e(TAG, "prepare format:" + format);
        if (format == null) {
            return 0;
        }
        if (mDecoder != null) {
            Log.e(TAG, "prepare decoder twice");
            return 0;
        }
        String mime = format.getString(MediaFormat.KEY_MIME);
        int type = 0;
        if (mime == null) {
            return type;
        } else if (mime.contains("video")) {
            type = 1;
        } else if (mime.contains("audio")) {
            type = 2;
        }
        mIsOutputSurface = surface != null && surface.isValid();
        mDecoder = MediaCodec.createDecoderByType(mime);
        mDecoder.setCallback(mCallback);
        mDecoder.configure(format, surface, null, 0);
        flushAndStart();
        return type;
    }

    public void flushAndStart() {
        mInputPool.clear();
        mOutputPool.clear();
        mDecoder.flush();
        mDecoder.start();
    }

    public void decode(DecodeInfo info, int offset, int size, long presentationTimeUs, int flags) {
        if (mDecoder == null || info == null) {
            Log.e(TAG, "decode error, mDecoder:" + mDecoder + ", data:" + info);
            return;
        }
        mDecoder.queueInputBuffer(info.index, offset, size, presentationTimeUs, flags);
        info.reset();
        mInputPool.free(info);
    }

    public void release() {
        if (mDecoder != null) {
            mDecoder.stop();
            mDecoder.setCallback(null);
            mDecoder.release();
            mDecoder = null;
        }
    }

    MediaCodec.Callback mCallback = new MediaCodec.Callback() {
        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
            try {
                offerInputInfo(mInputPool.getFreeData().setIndex(index).setBuffer(codec.getInputBuffer(index)));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
            DecodeInfo outputInfo = mOutputPool.getFreeData().setIndex(index);
            if (!mIsOutputSurface) {
                outputInfo.setBuffer(codec.getOutputBuffer(index));
            }
            try {
                offerOutputInfo(outputInfo);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {
            Log.e(TAG, "onError:" + codec + ", " + android.util.Log.getStackTraceString(e));
        }

        @Override
        public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {
            Log.e(TAG, "onOutputFormatChanged, codec:" + codec + ", format:" + format);
            setFileFormat(format);
        }
    };
}
