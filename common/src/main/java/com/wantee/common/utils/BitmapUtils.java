package com.wantee.common.utils;

import android.graphics.BitmapFactory;

public class BitmapUtils {

    /**
     * 判断文件是否损坏
     *
     * @param width
     * @param filepath
     * @return
     */
    public static boolean checkImageIsDamage(int width, String filepath) {
        if (width == 0) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filepath, options); //filePath代表图片路径
            //表示图片已损毁
            return options.mCancel || options.outWidth == -1 || options.outHeight == -1;
        }
        return false;
    }
}
