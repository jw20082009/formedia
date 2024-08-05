package com.wantee.camera;

import android.view.Surface;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class Config {

    private EquipmentEnum mEquipmentType;
    private JSONObject mCaptureRequestObj;
    private List<Surface> mSurfaceList;

    public Config(EquipmentEnum type, Surface surface) {
        setEquipment(type);
        addSurface(surface);
    }

    public Config setEquipment(EquipmentEnum type) {
        mEquipmentType = type;
        return this;
    }

    public Config addCaptureRequest(String key, String value) throws JSONException {
        if (mCaptureRequestObj == null) {
            mCaptureRequestObj = new JSONObject();
        }
        mCaptureRequestObj.put(key, value);
        return this;
    }

    public Config addCaptureRequest(String key, String value, int api) throws JSONException {
        if (mCaptureRequestObj == null) {
            mCaptureRequestObj = new JSONObject();
        }
        JSONObject apiObj = new JSONObject();
        apiObj.put("val", value);
        apiObj.put("api", api);
        mCaptureRequestObj.put(key, apiObj);
        return this;
    }

    public Config addSurface(Surface surface) {
        if (surface == null) {
            return this;
        }
        if (mSurfaceList == null) {
            mSurfaceList = new LinkedList<>();
        }
        mSurfaceList.add(surface);
        return this;
     }

     public Config setCaptureRequest(String configValue) throws JSONException {
        mCaptureRequestObj = new JSONObject(configValue);
        return this;
     }

     public EquipmentEnum getEnum() {
        return mEquipmentType;
     }

     public List<Surface> getSurfaceList() {
        return mSurfaceList;
     }

     public JSONObject getCaptureRequestObj() {
        return mCaptureRequestObj;
     }
}
