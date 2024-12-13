package com.wantee.common;

public class Frame<T> {

    int mWidth;
    int mHeight;
    T mData;

    public Frame(T data, int width, int height) {
        mData = data;
        mWidth = width;
        mHeight = height;
    }

    public int getWidth() {
        return mWidth;
    }

    public Frame<T> setWidth(int mWidth) {
        this.mWidth = mWidth;
        return this;
    }

    public int getHeight() {
        return mHeight;
    }

    public Frame<T> setHeight(int mHeight) {
        this.mHeight = mHeight;
        return this;
    }

    public T getData() {
        return mData;
    }

    public Frame<T> setData(T data) {
        mData = data;
        return this;
    }
}
