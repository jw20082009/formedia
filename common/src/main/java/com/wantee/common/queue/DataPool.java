package com.wantee.common.queue;

import java.util.LinkedList;

public class DataPool <T>{

    private final LinkedList<T> mData = new LinkedList<>();
    private final LinkedList<T> mFreeData = new LinkedList<>();
    private int mMaxSize = 0;
    private int mMaxFreeSize = 0;
    private final PoolListener<T> mListener;
    private int mCreateIndex = 0;

    public DataPool(int maxSize, PoolListener<T> listener) {
        setMaxSize(maxSize);
        mListener = listener;
    }

    public void setMaxSize(int maxSize) {
        mMaxSize = maxSize;
        mMaxFreeSize = Math.max(1, maxSize / 2);
    }

    public void offer(T data) {
        synchronized (mData) {
            mData.offer(data);
            if (mData.size() > mMaxSize) {
                T freeData = mData.poll();
                if (mListener != null) {
                    mListener.onDrop(freeData);
                }
                free(freeData);
            }
        }
    }

    public T poll() {
        synchronized (mData) {
            return mData.poll();
        }
    }

    public void free(T data) {
        synchronized (mFreeData) {
            if (mFreeData.size() < mMaxFreeSize) {
                mFreeData.offer(data);
            }
        }
    }

    public T getFreeData() {
        synchronized (mFreeData) {
            T data = null;
            if (mFreeData.isEmpty()) {
                if (mListener != null) {
                    data = mListener.onCreate(mCreateIndex++);
                }
            } else {
                data = mFreeData.poll();
            }
            return data;
        }
    }

    public void clear() {
        synchronized (mData) {
            if (mListener != null) {
                for (T data : mData) {
                    mListener.onDrop(data);
                }
            }
            mData.clear();
        }
        synchronized (mFreeData) {
            mFreeData.clear();
        }
    }
}
