package com.wantee.render.drawer;

import android.content.Context;
import android.opengl.GLES20;

import com.wantee.common.Frame;
import com.wantee.render.utils.OpenGLUtils;

public class TransformDrawer extends BaseDrawer<Integer>{
    protected int mInputTextureHandle = -1;
    protected int mTransformMatrixHandle;
    private float[] mTransformMatrix = OpenGLUtils.getOriginalMatrix();

    public TransformDrawer(Context context, boolean needFrameBuffer) {
        super(OpenGLUtils.getShaderFromAssets(context, "shader/vertex_oes_input.glsl"),
                OpenGLUtils.getShaderFromAssets(context, "shader/fragment_normal.glsl"), needFrameBuffer);
    }

    public TransformDrawer(String vertexShader, String fragmentShader, boolean needFrameBuffer) {
        super(vertexShader, fragmentShader, needFrameBuffer);
    }

    public TransformDrawer(Context context) {
        this(context, false);
    }

    @Override
    protected boolean onInit(int program) {
        mInputTextureHandle = GLES20.glGetUniformLocation(program, "inputTexture");
        mTransformMatrixHandle = GLES20.glGetUniformLocation(program, "transformMatrix");
        return true;
    }

    @Override
    protected Frame<Integer> onDraw(Frame<Integer> frame) {
        float[] transformMatrix = mTransformMatrix;
        if (transformMatrix != null) {
            GLES20.glUniformMatrix4fv(mTransformMatrixHandle, 1, false, transformMatrix, 0);
        }
        // 绑定纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(getTextureType(), frame.getData());
        GLES20.glUniform1i(mInputTextureHandle, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, OpenGLUtils.getVertexCount());
        // 解绑
        GLES20.glBindTexture(getTextureType(), 0);
        return frame.setData((mFrameBufferTextures!= null && mFrameBufferTextures.length > 0) ? mFrameBufferTextures[0] : frame.getData());
    }

    public void setTransformMatrix(float[] transformMatrix) {
        mTransformMatrix = transformMatrix;
    }

    protected int getTextureType() {
        return GLES20.GL_TEXTURE_2D;
    }

}
