package com.wantee.formedia.ui;

import android.Manifest;
import android.graphics.SurfaceTexture;
import android.media.ImageReader;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wantee.camera.CameraContext;
import com.wantee.camera.EquipmentType;
import com.wantee.camera.preview.CameraListener;
import com.wantee.camera.preview.CameraStatusListener;
import com.wantee.camera.preview.ImageReaderListener;
import com.wantee.camera.preview.SurfaceTextureListener;
import com.wantee.common.log.Log;
import com.wantee.formedia.R;
import com.wantee.player.video.RenderSource;
import com.wantee.player.video.ViewRenderer;

import java.io.IOException;
import java.util.Map;

public class CameraFragment extends PermissionFragment {
    private final String TAG = "CameraFragment";
    private ViewRenderer<?> mRenderer = null;
    private CameraListener<?> mCameraListener = null;

    private <T> void switchCameraOutputType(Class<T> clazz, CameraStatusListener statusListener) {
        if (clazz == SurfaceTexture.class) {
            ViewRenderer<SurfaceTexture> renderer = new ViewRenderer<>(new RenderSource.SurfaceTextureSource());
            mCameraListener = new SurfaceTextureListener(statusListener) {

                @Override
                public SurfaceTexture onCreateDestination(int previewWidth, int previewHeight) {
                    return renderer.onSizeChanged(previewWidth, previewHeight);
                }
            };
            mRenderer = renderer;
        } else if (clazz == ImageReader.class) {
            ViewRenderer<ImageReader> renderer = new ViewRenderer<>(new RenderSource.ImageReaderSource());
            mCameraListener = new ImageReaderListener(statusListener) {

                @Override
                public ImageReader onCreateDestination(int previewWidth, int previewHeight) {
                    return renderer.onSizeChanged(previewWidth, previewHeight);
                }
            };
            mRenderer = renderer;
        }
    }

    @Override
    public String[] obtainRequiredPermissions() {
        String[] permissions = new String[] { Manifest.permission.CAMERA };
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            permissions = new String[] {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE };
        }
        return permissions;
    }

    @Override
    public void onPermissionResult(Map<String, Boolean> result) {
        if (result == null) {
            return;
        }
        Boolean cameraResult = result.get(Manifest.permission.CAMERA);
        if (cameraResult != null && cameraResult) {
            CameraContext.Instance.open(EquipmentType.Front_Camera2, 361, 640, mCameraListener);
            try {
                getActivity().getExternalFilesDir(null).createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_camera, container, false);
        GLSurfaceView surfaceView = root.findViewById(R.id.glsurfaceview);
        switchCameraOutputType(ImageReader.class, mStatusListener);
        mRenderer.setView(surfaceView);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        mRenderer.onResume();
        requestPermission();
    }

    @Override
    public void onPause() {
        super.onPause();
        mRenderer.onPause();
        CameraContext.Instance.close();
    }

    CameraStatusListener mStatusListener = new CameraStatusListener() {
        @Override
        public void onOpened(int requestCode, int cameraDegree, int displayRotate, boolean isFacingFront) {
            Log.e(TAG, "onOpened, cameraDegree:" + cameraDegree + ", displayRotate:" + displayRotate + ", isFacingFront:" + isFacingFront);
            mRenderer.setDisplayDegree((360 - cameraDegree - displayRotate) % 360, false, !isFacingFront);
        }

        @Override
        public void onError(int requestCode, String errorMessage) {
            Log.e(TAG, errorMessage);
        }

        @Override
        public void onClosed(int requestCode) {
            Log.e(TAG, "onClosed");
            mRenderer.releaseSource();
        }
    };
}
