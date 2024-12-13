package com.wantee.camera.device;

import android.util.Size;

import com.wantee.common.log.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 以采集分辨率为画布，剧中裁剪出期望分辨率的大小，主要参考裁剪率和缩放率来查找最优分辨率
 */
public class SizeChooser {
    private final String TAG = "SizeChooser";
    float cropWeight = 1.0f; // 裁剪率权重，裁剪越多代表比例相差越大
    float scaleWeight = 1.0f; // 缩放率权重，缩放越多代表面积相差越大

    /**
     * 根据裁剪率+缩放率来对分辨率评分，评分最低者最优
     * 裁剪率方程：y = x  (裁剪率为0时评分为0，裁剪率为1时评分为1)
     * 缩放率方程：y = x / 50 - 1/50 （缩放率为1时评分为0，缩放率为50时评分为1）,50是假定的最大缩放倍数，期望分辨率与真实分辨率之前的倍数差不应该超过50
     * @param preferSize
     * @param captureSize
     * @return
     */
    private float scoreSize(Size preferSize, Size captureSize) {
        float preferRatio = 1.0f * preferSize.getWidth() / preferSize.getHeight();
        float captureRatio = 1.0f * captureSize.getWidth() / captureSize.getHeight();
        float cropRatio = 0f;
        float scaleRatio = 0f;
        if (preferRatio < captureRatio) {
            // 宽被裁
            float scaledWidth = preferSize.getHeight() * captureRatio;
            cropRatio = (scaledWidth - preferSize.getWidth()) / scaledWidth;
            scaleRatio = captureSize.getWidth() / scaledWidth;
        } else if (preferRatio > captureRatio) {
            // 高被裁
            float scaledHeight = preferSize.getWidth() / captureRatio;
            cropRatio = (scaledHeight - preferSize.getHeight()) / scaledHeight;
            scaleRatio = captureSize.getHeight() / scaledHeight;
        } else {
            cropRatio = 0f;
            scaleRatio = 1.0f* captureSize.getWidth() / preferSize.getWidth();
        }
        return cropRatio * cropWeight + (0.01f* scaleRatio - 0.01f) * scaleWeight;
    }

    public Size onSelectPreviewSize(int preferWidth, int preferHeight, Size[] supportedPreviewSizes) {
        Size preferSize = new Size(preferHeight, preferWidth);
        List<Size> insufficient = new ArrayList<>();
        List<Size> sufficient = new ArrayList<>();
        for(Size size: supportedPreviewSizes) {
            if (size.getWidth() < preferSize.getWidth() || size.getHeight() < preferSize.getHeight()) {
                insufficient.add(size);
            } else {
                sufficient.add(size);
            }
        }
        if (sufficient.isEmpty()) {
            return insufficient.isEmpty()? null: insufficient.get(0);
        }
        Collections.sort(sufficient, (size0, size1) -> {
            float score0 = scoreSize(preferSize, size0);
            float score1 = scoreSize(preferSize, size1);
            if(score0 > score1) {
                return 1;
            } else if (size0 == size1) {
                return 0;
            }
            return -1;
        });
        return sufficient.get(0);
    }
}