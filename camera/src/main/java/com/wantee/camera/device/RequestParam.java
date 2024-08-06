package com.wantee.camera.device;

import android.graphics.Rect;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.MeteringRectangle;
import android.os.Build;
import android.text.TextUtils;
import android.util.Range;
import android.util.Size;

import com.wantee.common.log.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;

public class RequestParam {

    interface IParamChecker<T> {
        boolean isValid(T data);
    }

    public abstract static class BaseParam<T> {
        private final String mKeyStr;
        private IParamChecker<T> mChecker;
        private CaptureRequest.Key<T> mKey;
        private T mValue;
        public BaseParam(String key) {
            mKeyStr = key;
        }

        public void setChecker(IParamChecker<?> checker) {
            mChecker = null;
            if (checker != null) {
                mChecker = (IParamChecker<T>) checker;
            }
        }

        public String getKeyStr() { return mKeyStr; }

        public void setParam(CaptureRequest.Key<T> key, T value) {
            mKey = key;
            mValue = value;
        }

        public boolean configBuilder(CaptureRequest.Builder builder) {
            if (mKey != null && isValidParam(mValue)) {
                builder.set(mKey, mValue);
                Log.e(TAG, "configBuilder key:" + mKeyStr + "," + mValue);
                return true;
            }
            Log.e(TAG, "configBuilder key:" + mKeyStr + "," + mValue +" failed, "+ mKey);
            return false;
        }

        public T readValue(JSONObject obj, Object keyObj) {
            JSONObject apiObj = obj.optJSONObject(mKeyStr);
            int api = Build.VERSION_CODES.LOLLIPOP;
            if (apiObj != null) {
                api = apiObj.optInt("api");
            }
            if (Build.VERSION.SDK_INT < api) {
                return null;
            }
            T value = null;
            if (apiObj != null && apiObj.has("val")) {
                value = readParam(apiObj, "val");
            } else if(obj.has(mKeyStr)){
                value = readParam(obj, mKeyStr);
            }
            mValue = value;
            if (keyObj != null) {
                mKey = (CaptureRequest.Key<T>) keyObj;
            }
            return value;
        }

        public T getValue() {
            return mValue;
        }

        protected boolean isValidParam(T value) {
            if (value == null) {
                return false;
            }
            if (mChecker == null) {
                return true;
            }
            return mChecker.isValid(value);
        }

        protected abstract T readParam(JSONObject obj, String key);
    }
    static class IntParam extends BaseParam<Integer> {
        public IntParam(String key) {
            super(key);
        }

        @Override
        protected Integer readParam(JSONObject obj, String key) {
            return obj.optInt(key);
        }

    }

    static class BoolParam extends BaseParam<Boolean> {

        public BoolParam(String key) {
            super(key);
        }

        @Override
        protected Boolean readParam(JSONObject obj, String key) {
            return obj.optBoolean(key);
        }
    }

    static class FloatParam extends BaseParam<Float> {

        public FloatParam(String key) {
            super(key);
        }

        @Override
        protected Float readParam(JSONObject obj, String key) {
            return (float) obj.optDouble(key);
        }
    }

    static class StringParam extends BaseParam<String> {

        public StringParam(String key) {
            super(key);
        }

        @Override
        protected String readParam(JSONObject obj, String key) {
            return obj.optString(key);
        }
    }

    static class LongParam extends BaseParam<Long> {

        public LongParam(String key) {
            super(key);
        }

        @Override
        protected Long readParam(JSONObject obj, String key) {
            return obj.optLong(key);
        }
    }

    static class SizeParam extends BaseParam<Size> {

        public SizeParam(String key) {
            super(key);
        }

        @Override
        protected Size readParam(JSONObject obj, String key) {
            JSONArray array = obj.optJSONArray(key);
            if (array != null && array.length() >= 2) {
                return new Size(array.optInt(0), array.optInt(1));
            }
            return null;
        }
    }

    static class RangeParam extends BaseParam<Range<Integer>> {

        public RangeParam(String key) {
            super(key);
        }

        @Override
        protected Range<Integer> readParam(JSONObject obj, String key) {
            JSONArray array = obj.optJSONArray(key);
            if (array != null && array.length() >= 2) {
                return new Range<>(array.optInt(0), array.optInt(1));
            }
            return null;
        }
    }

    static class FloatArrayParam extends BaseParam<float[]> {

        public FloatArrayParam(String key) {
            super(key);
        }

        @Override
        protected float[] readParam(JSONObject obj, String key) {
            JSONArray array = obj.optJSONArray(key);
            if (array != null) {
                int length = array.length();
                if (length == 0) {
                    return null;
                }
                float[] result = new float[length];
                for ( int i = 0; i < length; i++) {
                    result[i] = (float) array.optDouble(i);
                }
                return result;
            }
            return null;
        }
    }

    static class IntArrayParam extends BaseParam<int[]> {

        public IntArrayParam(String key) {
            super(key);
        }

        @Override
        protected int[] readParam(JSONObject obj, String key) {
            JSONArray array = obj.optJSONArray(key);
            if (array != null) {
                int length = array.length();
                if (length == 0) {
                    return null;
                }
                int[] result = new int[length];
                for ( int i = 0; i < length; i++) {
                    result[i] = array.optInt(i);
                }
                return result;
            }
            return null;
        }
    }

    static class MeteringRectangleArrayParam extends BaseParam<MeteringRectangle[]> {

        public MeteringRectangleArrayParam(String key) {
            super(key);
        }

        @Override
        protected MeteringRectangle[] readParam(JSONObject obj, String key) {
            JSONArray array = obj.optJSONArray(key);
            if (array != null) {
                int length = array.length();
                if (length < 5) {
                    return null;
                }
                int size = length / 5;
                MeteringRectangle[] result = new MeteringRectangle[size];
                for ( int i = 0; i < length; i++) {
                    int start = i * 5;
                    MeteringRectangle rectangle = new MeteringRectangle(array.optInt(start), array.optInt(start + 1),
                            array.optInt(start + 2), array.optInt(start + 3), array.optInt(start + 4));
                    result[i] = rectangle;
                }
                return result;
            }
            return new MeteringRectangle[0];
        }
    }

    static class RectParam extends BaseParam<Rect> {

        public RectParam(String key) {
            super(key);
        }

        @Override
        protected Rect readParam(JSONObject obj, String key) {
            JSONArray array = obj.optJSONArray(key);
            if (array != null) {
                int length = array.length();
                if (length < 4) {
                    return null;
                }
                return new Rect(array.optInt(0), array.optInt(1), array.optInt(2), array.optInt(3));
            }
            return new Rect();
        }
    }

    private static final String TAG = "HECameraConfig";
    public static final String kTemplatePreview = "TEMPLATE_PREVIEW";
    public static final String kStreamUseCase = "STREAM_USE_CASE";
    public static BaseParam<?> createConfig(Class<?> clazz, String key) {
        if (clazz.equals(Integer.class)) {
            return new IntParam(key);
        } else if (clazz.equals(Boolean.class)) {
            return new BoolParam(key);
        } else if (clazz.equals(Float.class)) {
            return new FloatParam(key);
        } else if (clazz.equals(String.class)) {
            return new StringParam(key);
        } else if (clazz.equals(Long.class)) {
            return new LongParam(key);
        } else if (clazz.equals(Size.class)) {
            return new SizeParam(key);
        } else if (clazz.equals(Range.class)) {
            return new RangeParam(key);
        } else if (clazz.equals(float[].class)) {
            return new FloatArrayParam(key);
        } else if (clazz.equals(int[].class)) {
            return new IntArrayParam(key);
        } else if (clazz.equals(MeteringRectangle[].class)) {
            return new MeteringRectangleArrayParam(key);
        } else if (clazz.equals(Rect.class)) {
            return new RectParam(key);
        }
        Log.e(TAG, "createConfig:" + key + " failed");
        return null;
    }

    /**
     * @param configStr 配置参数以json字符串方式被解析，json中的每一个key对应于 android.hardware.camera2.CaptureRequest中的一个Key类型成员参数，例如："CONTROL_AE_TARGET_FPS_RANGE"对应于CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE
     * 默认情况下json中的每一个key所对应的值类型应该为：         int/float/boolean/long/String/int[]/float[]
     * 不同的值类型再结合key中的范型信息可以被对应解析到如下类型：  int/float/boolean/long/String/Size/Range/int[]/MeteringRectangle[]/float[]等等
     * 例如想指定"CONTROL_AE_TARGET_FPS_RANGE"为[15,20],则格式如下：
     * "CONTROL_AE_TARGET_FPS_RANGE":[15,20]
     * 当参数不是适用于所有版本时，可以给对应参数指定api，低于api的设备将自动不会执行该下发，存在api的参数其值类型应当为一个JSONObject,包含"val"和"api"两个值
     * 例如想指定"CONTROL_AE_TARGET_FPS_RANGE"为[15,20],并指定api为30，则格式如下：
     * "CONTROL_AE_TARGET_FPS_RANGE":{
     *                                  "val":[15,20],
     *                                  "api":30
     *                               }
     *
     * @param configs 用于接收被解析完成的配置列表
     * @return 如果配置参数中包含TEMPLATE_PREVIEW相关值，则会返回templateType(可被用于提前创建CaptureRequest.Builder)，如果不存在则返回null
     */
    public static int parseParam(String configStr, List<BaseParam<?>> configs) throws JSONException, NoSuchFieldException, IllegalAccessException {
        if (TextUtils.isEmpty(configStr)) {
            return -1;
        }
        JSONObject jsonObject = new JSONObject(configStr);
        Iterator<String> keyIterator = jsonObject.keys();
        int templateType = -1;
        while(keyIterator.hasNext()) {
            String key = keyIterator.next();
            if (TextUtils.equals(kTemplatePreview, key)) {
                IntParam templateParam = (IntParam) createConfig(Integer.class, key);
                templateType = templateParam.readValue(jsonObject, null);
            } else {
                if (configs == null) {
                    continue;
                }
                Field field = CaptureRequest.class.getField(key);
                field.setAccessible(true);
                Object obj = field.get(null);
                if (obj instanceof CaptureRequest.Key) {
                    Type genericFieldType = field.getGenericType();
                    if (genericFieldType instanceof ParameterizedType) {
                        ParameterizedType aType = (ParameterizedType) genericFieldType;
                        Type[] fieldArgTypes = aType.getActualTypeArguments();
                        if (fieldArgTypes.length == 0) {
                            continue;
                        }
                        Class<?> fieldClass = null;
                        if (fieldArgTypes[0] instanceof ParameterizedType) {
                            ParameterizedType bType = (ParameterizedType) fieldArgTypes[0];
                            fieldClass = (Class<?>) bType.getRawType();
                            Log.e(TAG, "parseConfig:" + key + ";length:" + fieldArgTypes.length + ";fieldClass:" + fieldClass);
                        } else if (fieldArgTypes[0] instanceof Class){
                            Log.e(TAG, "parseConfig fieldClass:" + fieldArgTypes[0]);
                            fieldClass = (Class<?>) fieldArgTypes[0];
                        }
                        if (fieldClass != null) {
                            BaseParam<?> config = createConfig(fieldClass, key);
                            if (config != null) {
                                config.readValue(jsonObject, obj);
                                configs.add(config);
                            }
                        }
                    }
                }
            }
        }
        return templateType;
    }

    public static String createTestConfig() throws JSONException {
        /**
         * {
         *      "CONTROL_AE_TARGET_FPS_RANGE":{
         *                                      "val":[15,20],
         *                                      "api":30
         *                                    },
         *      "STATISTICS_FACE_DETECT_MODE":0,
         *      "CONTROL_VIDEO_STABILIZATION_MODE":0,
         *      "LENS_OPTICAL_STABILIZATION_MODE":0
         * }
         */
        JSONObject object = new JSONObject();
        JSONObject fpsObj = new JSONObject();
        JSONArray array = new JSONArray();
        array.put(10);
        array.put(30);
        fpsObj.put("val", array);
        fpsObj.put("api", 34);
        object.put("CONTROL_AE_TARGET_FPS_RANGE", fpsObj);
        object.put("STATISTICS_FACE_DETECT_MODE", CaptureRequest.STATISTICS_FACE_DETECT_MODE_OFF);
        object.put("CONTROL_VIDEO_STABILIZATION_MODE", CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_OFF);
        object.put("LENS_OPTICAL_STABILIZATION_MODE", CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_OFF);
        String testParam = object.toString();
        android.util.Log.e(TAG, "createTestConfig:" + testParam);
        return testParam;
    }
}
