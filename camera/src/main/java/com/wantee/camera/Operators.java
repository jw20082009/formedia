package com.wantee.camera;

import org.json.JSONException;
import org.json.JSONObject;

public class Operators {
    public JSONObject captureRequestObj;
    private JSONObject getCaptureRequestObj() {
        if (captureRequestObj == null) {
            captureRequestObj = new JSONObject();
        }
        return captureRequestObj;
    }

    public JSONObject addIntRequest(String key, int value) throws JSONException {
        getCaptureRequestObj().put(key, value);
        return captureRequestObj;
    }

    public JSONObject addStringRequest(String key, String value) throws JSONException {
        getCaptureRequestObj().put(key, value);
        return captureRequestObj;
    }
}
