package com.wantee.player.video;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.media.Image;
import android.media.ImageReader;
import android.opengl.GLES11Ext;

import com.wantee.common.handler.CommonHandler;
import com.wantee.common.log.Log;
import com.wantee.common.queue.DataPool;
import com.wantee.common.queue.PoolListener;
import com.wantee.common.utils.SurfaceTextureUtils;
import com.wantee.render.RenderMode;
import com.wantee.common.Frame;
import com.wantee.render.Rotation;
import com.wantee.render.drawer.TransformDrawer;
import com.wantee.render.drawer.YuvDrawer;
import com.wantee.render.utils.OpenGLUtils;

import java.nio.ByteBuffer;

public class RenderSource {

    public interface ImageListener<T> {
        void onImageAvailable(T imageWrapper);
    }

    public interface Source<T> {
        void releaseSource();
        void releaseRender();
        void initRender(Context context);
        T onSizeChanged(int width, int height);
        void setDisplayDegree(int degree, boolean flipHorizontal, boolean flipVertical);
        Frame<Integer> onRenderTexture();
        void setImageListener(ImageListener<T> listener);
        int getWidth();
        int getHeight();
    }

    public static abstract class BaseSource<T> implements Source<T> {
        private final String TAG = "BaseSource";
        protected ImageListener<T> mListener;
        protected int mImageWidth = 0;
        protected int mImageHeight = 0;
        protected int mDisplayDegree = 0;
        protected boolean mFlipVertical = false;
        protected boolean mFlipHorizontal = false;
        protected T mFrameWrapper;

        @Override
        public void setImageListener(ImageListener<T> listener) {
            mListener = listener;
        }

        @Override
        public void setDisplayDegree(int degree, boolean flipHorizontal, boolean flipVertical) {
            mDisplayDegree = degree;
            mFlipHorizontal = flipHorizontal;
            mFlipVertical = flipVertical;
        }

        @Override
        public T onSizeChanged(int width, int height) {
            Log.e(TAG, "onSizeChanged:" + mImageWidth + "*" + mImageHeight + "=>" + width + "*" + height);
            if (mFrameWrapper == null || mImageWidth != width || mImageHeight != height) {
                mFrameWrapper = onCreateFrameWrapper(width, height);
                mImageWidth = width;
                mImageHeight = height;
            }
            return mFrameWrapper;
        }

        public void notifyImage(T imageWrapper) {
            if (mListener != null) {
                mListener.onImageAvailable(imageWrapper);
            }
        }

        @Override
        public int getWidth() {
            return mImageWidth;
        }

        @Override
        public int getHeight() {
            return mImageHeight;
        }

        protected abstract T onCreateFrameWrapper(int width, int height);
    }

    static int i = 0;

    public static class SurfaceTextureSource extends BaseSource<SurfaceTexture> {

        private final String TAG = "SurfaceTextureSource";
        private int mOESTextureId = -1;
        private final float[] mSTMatrix = new float[16];
        private TransformDrawer mOesDrawer;

        @Override
        public void releaseSource() {
            Log.e(TAG, "releaseRender releaseSource");
            SurfaceTextureUtils.Camera.release();
            mFrameWrapper = null;
        }

        @Override
        public void releaseRender() {
            Log.e(TAG, "releaseRender,mOESTextureId:" + mOESTextureId + ",mOesDrawer:" + mOesDrawer);
            if (mOESTextureId != -1) {
                OpenGLUtils.deleteTexture(mOESTextureId);
                mOESTextureId = -1;
            }
            if (mOesDrawer != null) {
                mOesDrawer.release();
                mOesDrawer = null;
            }
            SurfaceTextureUtils.Camera.detachFromGLContext();
        }

        @Override
        public void initRender(Context context) {
            mOESTextureId = -1; // 非SharedContext时所有texId会在onSurfaceCreated之后重置
            mOesDrawer = new TransformDrawer(OpenGLUtils.getShaderFromAssets(context, "shader/vertex_oes_input.glsl"),
                    OpenGLUtils.getShaderFromAssets(context, "shader/fragment_oes_input.glsl"), true) {
                @Override
                protected int getTextureType() {
                    return GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
                }
            };
        }

        @Override
        public Frame<Integer> onRenderTexture() {
            Frame<Integer> texId = null;
            try {
                SurfaceTexture surfaceTexture = SurfaceTextureUtils.Camera.get(1000);
                if (surfaceTexture != null) {
                    if (mOESTextureId == -1) {
                        Log.e(TAG, "onRenderTexture createOESTexture & attachToGLContext & setOnFrameAvailableListener");
                        mOESTextureId = OpenGLUtils.createOESTexture();
                        SurfaceTextureUtils.Camera.attachToGLContext(mOESTextureId);
                        SurfaceTextureUtils.Camera.setOnFrameAvailableListener(mFrameAvailableListener, CommonHandler.instance().handler());
                    }
                    surfaceTexture.updateTexImage();
                    surfaceTexture.getTransformMatrix(mSTMatrix);
                    int w = mImageHeight;
                    int h = mImageWidth;
                    if (mOesDrawer != null && w > 0 && h > 0) {
                        mOesDrawer.init();
                        mOesDrawer.setDisplaySize(w, h);
                        mOesDrawer.setRenderMode(RenderMode.CenterCrop);
                        mOesDrawer.setTransformMatrix(mSTMatrix);
                        texId = mOesDrawer.drawFrame(new Frame<>(mOESTextureId, w, h));
//                        texId = mOesDrawer.drawFrame(new Frame<>(mOESTextureId, mImageHeight, mImageWidth), new Runnable() {
//                            @Override
//                            public void run() {
//                                if (i == 100) {
//                                    OpenGLUtils.readToPng(mImageHeight, mImageWidth, "/sdcard/Android/data/com.wantee.formedia/test.png");
//                                }
//                                i++;
//                            }
//                        });
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return texId;
        }

        SurfaceTexture.OnFrameAvailableListener mFrameAvailableListener = this::notifyImage;

        @Override
        protected SurfaceTexture onCreateFrameWrapper(int width, int height) {
            Log.e(TAG, "onCreateFrameWrapper:" + width + "*" + height);
            return SurfaceTextureUtils.Camera.getOrCreate(width, height);
        }
    }

    public static class ImageReaderSource extends BaseSource<ImageReader> {
        private final String TAG = "ImageReaderSource";
        private int mLogLimit = 0;
        private YuvDrawer mYuvDrawer;
        private ByteBuffer mYuvDataBuffer;
        private final DataPool<byte[]> mFramePool = new DataPool<>(1, new PoolListener<byte[]>() {
            @Override
            public byte[] onCreate(int id) {
                return new byte[mImageWidth * mImageHeight * 3 / 2];
            }
        });

        @Override
        public void releaseSource() {
            Log.e(TAG, "releaseSource:" + mFrameWrapper);
            if (mFrameWrapper != null) {
                mFrameWrapper.close();
                mFrameWrapper = null;
            }
            mFramePool.clear();
        }

        @Override
        public void releaseRender() {
            if (mYuvDrawer != null) {
                Log.e(TAG, "releaseRender mYuvDrawer.release");
                mYuvDrawer.release();
                mYuvDrawer = null;
            }
            if (mYuvDataBuffer != null) {
                Log.e(TAG, "releaseRender mYuvDataBuffer.clear");
                mYuvDataBuffer.clear();
                mYuvDataBuffer = null;
            }
        }

        @Override
        public void initRender(Context context) {
            if (mYuvDrawer == null) {
                Log.e(TAG, "initRender new YuvDrawer");
                mYuvDrawer = new YuvDrawer(context, true);
            }
        }

        @Override
        public Frame<Integer> onRenderTexture() {
            Frame<Integer> texId = null;
            byte[] frame = mFramePool.poll();
            if (frame != null) {
                if(mYuvDataBuffer == null || mYuvDataBuffer.capacity() < frame.length) {
                    mYuvDataBuffer = ByteBuffer.allocateDirect(frame.length);
                }
                try {
                    mYuvDataBuffer.position(0);
                    mYuvDataBuffer.put(frame, 0, frame.length);
                    mFramePool.free(frame);
                } catch (Exception e) {
                    Log.e(TAG, android.util.Log.getStackTraceString(e));
                }
                if (mYuvDrawer != null && mImageWidth > 0 && mImageHeight > 0) {
                    mYuvDrawer.init();
                    mYuvDrawer.setDisplaySize(mImageWidth, mImageHeight);
                    mYuvDrawer.setRenderMode(RenderMode.CenterCrop);
                    mYuvDrawer.setDisplayDegree(Rotation.getRotation(mDisplayDegree), mFlipHorizontal, mFlipVertical);
                    texId = mYuvDrawer.drawFrame(new Frame<>(mYuvDataBuffer, mImageWidth, mImageHeight), new Runnable() {
                        @Override
                        public void run() {
                            if (i == 100) {
                                OpenGLUtils.readToPng(mImageWidth, mImageHeight, "/sdcard/Android/data/com.wantee.formedia/test.png");
                            }
                            i++;
                        }
                    });
                }
            }
            return texId;
        }

        private byte[] retrieveI420p(Image image) {
            int width = image.getWidth();
            int height = image.getHeight();
            if (mImageWidth != width || mImageHeight != height) {
                mFramePool.clear();
                mImageWidth = width;
                mImageHeight = height;
            }
            byte[] bufData = mFramePool.getFreeData();
            int pixelStride, rowStride;
            final Image.Plane[] planes = image.getPlanes();
            int offset = 0;
            for (int i = 0; i < planes.length; i++) {
                pixelStride = planes[i].getPixelStride();
                rowStride = planes[i].getRowStride();
                ByteBuffer buffer = planes[i].getBuffer();
                buffer.position(0);
                int realWidth = width;
                int realHeight = height;
                if (i != 0) {
                    realWidth = width / 2;
                    realHeight = height / 2;
                }
                if (pixelStride == 1 && realWidth == rowStride) {
                    buffer.get(bufData, offset, realWidth * realHeight);
                    offset += realWidth * realHeight;
                } else {
                    byte[] bytes = new byte[rowStride];
                    for (int row = 0; row < realHeight - 1; row++) {
                        buffer.get(bytes, 0, rowStride);
                        for (int column = 0; column < realWidth; column++) {
                            bufData[offset++] = bytes[pixelStride * column];
                        }
                    }
                    buffer.get(bytes, 0, Math.min(rowStride, buffer.remaining()));
                    for (int column = 0; column < realWidth; column++) {
                        bufData[offset++] = bytes[pixelStride * column];
                    }
                }
            }
            return bufData;
        }

        ImageReader.OnImageAvailableListener mFrameAvailableListener = reader -> {
            Image image = reader.acquireLatestImage();
            if (image == null) {
                return;
            }
            try {
                byte[] frame = retrieveI420p(image);
                if (i % 50 == 0) {
                    OpenGLUtils.saveByteDirect(frame, "/sdcard/Android/data/com.wantee.formedia/test" + mImageWidth + "_" + mImageHeight + ".yuv");
                }
                i ++;
                mFramePool.offer(frame);
            } catch (Exception e) {
                if (mLogLimit % 50 == 0) {
                    Log.e(TAG, "onImageAvailableErr:" + android.util.Log.getStackTraceString(e));
                    mLogLimit = 0;
                }
                mLogLimit ++;
            } finally {
                image.close();
            }
            notifyImage(reader);
        };

        @Override
        protected ImageReader onCreateFrameWrapper(int width, int height) {
            Log.e(TAG, "onCreateFrameWrapper:" + width + "*" + height);
            mFrameWrapper = ImageReader.newInstance(width, height, ImageFormat.YUV_420_888, 2);
            mFrameWrapper.setOnImageAvailableListener(mFrameAvailableListener, CommonHandler.instance().handler());
            return mFrameWrapper;
        }
    }

}
