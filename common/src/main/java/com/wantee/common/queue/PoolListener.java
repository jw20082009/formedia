package com.wantee.common.queue;

public abstract class PoolListener<T> {
    public abstract T onCreate(int id);
    public void onDrop(T data) {};
}
