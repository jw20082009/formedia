package com.wantee.camera.api2;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Build;
import android.util.Size;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.wantee.camera.CameraContext;
import com.wantee.camera.request.BaseCamera;
import com.wantee.camera.device.CameraChooser;
import com.wantee.camera.device.SizeChooser;
import com.wantee.camera.Previewer;
import com.wantee.camera.device.Status;
import com.wantee.common.Constant;
import com.wantee.common.log.Log;

import org.json.JSONException;

public class Camera2Impl extends BaseCamera {
    private final String TAG = "Camera2Impl";
    private final BugFix mBugFix = new BugFix(1);
    private CameraCharacteristics mDevCharacteristics;
    private CameraManager mManager;
    private String mDeviceId;
    private String mCaptureParam;
    private int mPreferWidth = 0;
    private int mPreferHeight = 0;
    private SizeChooser mSizeChooser;
    private CameraDevice mDevice;
    private CharacteristicsHelper mHelper;
    private CameraCaptureSession mCaptureSession;
    public Camera2Impl() {}

    public SizeChooser getSizeChooser() {
        if (mSizeChooser == null) {
            mSizeChooser = new SizeChooser();
        }
        return mSizeChooser;
    }

    private CameraCharacteristics getCharacteristics() throws CameraAccessException {
        if (mDevCharacteristics == null) {
            mDevCharacteristics = getCameraManager().getCameraCharacteristics(mDeviceId);
            mHelper = new CharacteristicsHelper(mDevCharacteristics);
        }
        return mDevCharacteristics;
    }

    private CameraManager getCameraManager() {
        Context context = CameraContext.Instance.getContext();
        if (mManager == null && context != null) {
            mManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        }
        if (mManager == null) {
            throw new RuntimeException("mManager == null");
        }
        return mManager;
    }

    public boolean isHardwareSupported() throws CameraAccessException {
        CameraCharacteristics characteristics = getCharacteristics();
        Integer level = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
        if (level != null && (level == CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED
                || level == CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_FULL
                || level == CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_3)) {
            return true;
        }
        Log.e(TAG, "Device support for api level is LEGACY, level" + level);
        throw new RuntimeException("HARDWARE_LEVEL:" + level);
    }

    private boolean isGetCameraPermission() {
        Context context = CameraContext.Instance.getContext();
        if (context != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "have no camera permission");
            throw new RuntimeException("NoCameraPermission");
        }
        return true;
    }

    CameraDevice.StateCallback mCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            Log.e(TAG, "onOpened deviceId:" + cameraDevice.getId());
            mDevice = cameraDevice;
            SizeChooser chooser = getSizeChooser();
            Previewer<?> previewer = onStartPreview();
            Size previewSize = chooser.onSelectPreviewSize(mPreferWidth, mPreferHeight, mHelper.getSupportedPreviewSize(previewer));
            Object destination = previewer.createDestination(previewSize.getWidth(), previewSize.getHeight());
            try {
                if (destination instanceof Surface) {
                    mHelper.startCaptureSession(mCaptureParam, cameraDevice, (Surface) destination, sessionStateCallback);
                } else {
                    Log.e(TAG, "preview destination error");
                }
            } catch (JSONException | NoSuchFieldException | IllegalAccessException |
                     CameraAccessException e) {
                onCameraError(android.util.Log.getStackTraceString(e));
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            onCameraError("onDisconnected deviceId:" + cameraDevice.getId());
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            onCameraError("onError SystemError:" + i);
        }
    };

    CameraCaptureSession.StateCallback sessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            try {
                Log.e(TAG, "onConfigured:" + session);
                mCaptureSession = session;
                repeatRequest();
                Camera2Impl.this.onDeviceOpened(mHelper.getWindowDegree());
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            onCameraError("onConfigureFailed");
            Log.e(TAG, "onConfigureFailed:" + session);
        }

        @Override
        public void onClosed(@NonNull CameraCaptureSession session) {
            super.onClosed(session);
            Log.e(TAG, "onClosed:" + session);
            mBugFix.clear();
            Camera2Impl.this.onDeviceClosed();
        }
    };

    @Override
    protected int openDevice(String deviceId, int width, int height, String captureRequest) throws CameraAccessException {
        Log.e(TAG, "openDevice:" + deviceId +"[" + width + "*" + height+"]," + captureRequest);
        setStatus(Status.Opening);
        mDeviceId = deviceId;
        mPreferWidth = width;
        mPreferHeight = height;
        mCaptureParam = captureRequest;
        if (isHardwareSupported() && isGetCameraPermission()) {
            getCameraManager().openCamera(deviceId, mCallback, null);
            return Constant.True;
        }
        return Constant.Error;
    }

    @Override
    protected int closeDevice() {
        if (mDevice != null) {
            Log.e(TAG, "closeDevice:" + mDeviceId);
            setStatus(Status.Closing);
            mDevice.close();
            mDevice = null;
        }
        return 0;
    }

    private void repeatRequest() throws CameraAccessException {
        if (mCaptureSession != null) {
            mCaptureSession.setRepeatingRequest(mHelper.getRequestBuilder().build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    mBugFix.onCaptureCompleted(result);
                }
            }, null);
        }
    }

    @Override
    protected CameraChooser createCameraChooser() {
        return new Camera2Chooser(getCameraManager());
    }
}
