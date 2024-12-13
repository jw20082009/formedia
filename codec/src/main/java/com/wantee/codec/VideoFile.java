package com.wantee.codec;

import android.media.MediaFormat;
import android.os.Build;

public class VideoFile {
    private int mFps = 0;
    private int mBitRate = 0;
    private int mDecodeWidth = 0;
    private int mDecodeHeight = 0;
    private int mDecodeRotation = 0;
    private int mChannelCount = 0;
    private int mSampleRate = 0;
    private long mDuration = 0;

    public int getFps() {
        return mFps;
    }

    public int getBitRate() {
        return mBitRate;
    }

    public int getDecodeWidth() {
        return mDecodeWidth;
    }

    public int getDecodeHeight() {
        return mDecodeHeight;
    }

    public int getDecodeRotation() {
        return mDecodeRotation;
    }

    public int getChannelCount() {
        return mChannelCount;
    }

    public int getSampleRate() {
        return mSampleRate;
    }

    public long getDuration() {
        return mDuration;
    }
    
    public void setFileFormat(MediaFormat format) {
        if (format == null) {
            return;
        }
        if (format.containsKey(MediaFormat.KEY_WIDTH))
            mDecodeWidth = format.getInteger(MediaFormat.KEY_WIDTH);
        if (format.containsKey(MediaFormat.KEY_HEIGHT))
            mDecodeHeight = format.getInteger(MediaFormat.KEY_HEIGHT);
        if (format.containsKey(MediaFormat.KEY_ROTATION) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mDecodeRotation = format.getInteger(MediaFormat.KEY_ROTATION);
        }
        if (format.containsKey(MediaFormat.KEY_CHANNEL_COUNT))
            mChannelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
        if (format.containsKey(MediaFormat.KEY_SAMPLE_RATE))
            mSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
        if (format.containsKey(MediaFormat.KEY_FRAME_RATE))
            mFps = format.getInteger(MediaFormat.KEY_FRAME_RATE);
        if (format.containsKey(MediaFormat.KEY_BIT_RATE))
            mBitRate = format.getInteger(MediaFormat.KEY_BIT_RATE);
        if (format.containsKey(MediaFormat.KEY_DURATION))
            mDuration = format.getLong(MediaFormat.KEY_DURATION);
    }
}
