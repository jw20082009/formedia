package com.wantee.common.queue;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

public class BlockPool <T> {

    private final LinkedBlockingQueue<T> mData;
    private final LinkedList<T> mFreeData = new LinkedList<>();
    private int mMaxFreeSize = 0;
    private final PoolListener<T> mListener;
    private int mCreateIndex = 0;

    public BlockPool(int maxSize, int maxFreeSize, PoolListener<T> listener) {
        mMaxFreeSize = maxFreeSize;
        mData = new LinkedBlockingQueue<>(maxSize);
        mListener = listener;
    }

    public void offer(T data) throws InterruptedException {
        mData.put(data);
    }

    public T poll() throws InterruptedException {
        return mData.take();
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
