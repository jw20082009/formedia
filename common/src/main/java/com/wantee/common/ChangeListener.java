package com.wantee.common;

public interface ChangeListener<T>{
    void onChanged(T oldOne, T newOne);
}
