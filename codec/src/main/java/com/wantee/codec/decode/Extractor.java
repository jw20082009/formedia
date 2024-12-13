package com.wantee.codec.decode;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.wantee.common.log.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Extractor {
    private final String TAG = "Extractor";
    public enum Type {
        AUDIO, VIDEO;
    }

    public static class ExtractData extends Decoder.DecodeInfo {
        public ByteBuffer buffer;
        public int size;
        public long time;
        public boolean last;

        public ExtractData(int index, ByteBuffer buffer) {
            super(index, buffer);
        }

        @NonNull
        @Override
        public String toString() {
            return "ExtractInfo[buffer:" + buffer + ",size:" + size + ",time:" + time + ",last:" + last + "]";
        }
    }

    private MediaExtractor mExtractor;
    private long mCurrentTimeUs = 0;

    public MediaFormat prepare(String filePath, Type type) throws IOException {
        Log.e(TAG, "prepare filepath:" + filePath);
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        if (mExtractor != null) {
            Log.e(TAG, "prepare twice filepath:" + filePath);
            return null;
        }
        MediaFormat result = null;
        mExtractor = new MediaExtractor();
        mExtractor.setDataSource(filePath);
        for (int i = 0; i < this.mExtractor.getTrackCount(); ++i) {
            MediaFormat format = this.mExtractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime == null) {
                Log.e(TAG, "prepare without mime, filepath:" + filePath);
                return null;
            }
            if ((type == Type.VIDEO && mime.startsWith("video/")) || (type == Type.AUDIO && mime.startsWith("audio/"))) {
                mExtractor.selectTrack(i);
                result = format;
                break;
            }
        }
        return result;
    }

    public int readBuffer(ExtractData info) {
        if (info == null || info.buffer == null || mExtractor == null) {
            Log.e(TAG, "fillBuffer when buffer:" + info+ ", mExtractor:" + mExtractor);
            return -1;
        }
        int size = mExtractor.readSampleData(info.buffer, 0);
        info.time = mExtractor.getSampleTime();
        info.size = size;
        if (size > 0) {
            mExtractor.advance();
            mCurrentTimeUs = mExtractor.getSampleTime();
        } else {
            info.last = true;
        }
        return size;
    }

    public long seekTo(long timeUs) {
        if (mExtractor == null || Math.abs(timeUs - mCurrentTimeUs) < 40000 /*40ms*/) {
            return mCurrentTimeUs;
        }
        long time = mCurrentTimeUs;
        int retryTimes = 10;
        do {
            mExtractor.seekTo(timeUs, android.media.MediaExtractor.SEEK_TO_CLOSEST_SYNC); /*seek到最接近的关键帧处*/
            time = mExtractor.getSampleTime();
        } while (time < 0 && --retryTimes > 0);
        mCurrentTimeUs = time;
        return mCurrentTimeUs;
    }

    public void release() {
        if (mExtractor != null) {
            mExtractor.release();
            mExtractor = null;
        }
    }
}
