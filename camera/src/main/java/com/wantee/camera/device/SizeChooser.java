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
     * 根据裁剪率+缩放率来对分辨率评分，评分最高者最优
     * @param preferSize
     * @param captureSize
     * @return
     */
    private float scoreSize(Size preferSize, Size captureSize) {
        float preferRatio = 1.0f * preferSize.getWidth() / preferSize.getHeight();
        float captureRatio = 1.0f * captureSize.getWidth() / captureSize.getHeight();
        float cropRatio = 1.0f;
        float scaleRatio = 1.0f;
        if (preferRatio < captureRatio) {
            // 宽被裁
            float scaledWidth = preferRatio * captureSize.getHeight();
            cropRatio = (captureSize.getWidth() - scaledWidth) / captureSize.getWidth();
            scaleRatio = scaledWidth / preferSize.getWidth();
        } else if (preferRatio > captureRatio) {
            // 高被裁
            float scaledHeight = captureSize.getWidth() / preferRatio;
            cropRatio = (captureSize.getHeight() - scaledHeight) / captureSize.getHeight();
            scaleRatio = scaledHeight / preferSize.getHeight();
        }
        return cropRatio * cropWeight + (scaleRatio - 1) * scaleWeight;
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