package com.wantee.render;

import android.graphics.Rect;

public enum RenderMode {
    FitCenter, CenterCrop, FitWidth, FitHeight, FitStart, FitEnd, Fill;

    public Rect layout(int dataWidth, int dataHeight, int displayWidth, int displayHeight) {
        return layout(dataWidth, dataHeight, displayWidth, displayHeight, 0, 0);
    }

    public Rect layout(int dataWidth, int dataHeight, int displayWidth, int displayHeight, int paddingX, int paddingY) {
        switch (this) {
            case FitCenter:
                return layoutFitCenter(dataWidth, dataHeight, displayWidth, displayHeight);
            case FitWidth:
                return layoutFitWidth(dataWidth, dataHeight, displayWidth, displayHeight);
            case FitHeight:
                return layoutFitHeight(dataWidth, dataHeight, displayWidth, displayHeight);
            case FitStart:
                return layoutFitStart(dataWidth, dataHeight, paddingX, paddingY);
            case FitEnd:
                return layoutFitEnd(dataWidth, dataHeight, displayWidth, displayHeight, paddingX, paddingY);
            case CenterCrop:
                return layoutCenterCrop(dataWidth, dataHeight, displayWidth, displayHeight);
            case Fill:
            default:
                return layoutFill(displayWidth, displayHeight);
        }
    }

    private Rect getCenterRect(int width, int height, int displayWidth, int displayHeight) {
        int left = (displayWidth - width) / 2;
        int top = (displayHeight - height) / 2;
        return new Rect(left, top, left + width, top + height);
    }

    private Rect layoutFitCenter(int dataWidth, int dataHeight, int displayWidth, int displayHeight) {
        float ratioData = 1.0f * dataWidth / dataHeight;
        float ratioDisplay = 1.0f * displayWidth / displayHeight;
        int width = displayWidth;
        int height = displayHeight;
        if (ratioData < ratioDisplay) {
            width = (int) (ratioData * displayHeight);
        } else if (ratioData > ratioDisplay) {
            height = (int) (displayWidth / ratioData);
        }
        return getCenterRect(width, height, displayWidth, displayHeight);
    }

    private Rect layoutCenterCrop(int dataWidth, int dataHeight, int displayWidth, int displayHeight) {
        float ratioData = 1.0f * dataWidth / dataHeight;
        float ratioDisplay = 1.0f * displayWidth / displayHeight;
        int width = displayWidth;
        int height = displayHeight;
        if (ratioData < ratioDisplay) {
            height = (int) (displayWidth / ratioData);
        } else if (ratioData > ratioDisplay) {
            width = (int) (ratioData * displayHeight);
        }
        return getCenterRect(width, height, displayWidth, displayHeight);
    }

    private Rect layoutFitWidth(int dataWidth, int dataHeight, int displayWidth, int displayHeight) {
        float ratioData = 1.0f * dataWidth / dataHeight;
        int height = (int) (displayWidth / ratioData);
        return getCenterRect(displayWidth, height, displayWidth, displayHeight);
    }

    private Rect layoutFitHeight(int dataWidth, int dataHeight, int displayWidth, int displayHeight) {
        float ratioData = 1.0f * dataWidth / dataHeight;
        int width = (int) (ratioData * displayHeight);
        return getCenterRect(width, displayHeight, displayWidth, displayHeight);
    }

    private Rect layoutFitStart(int dataWidth, int dataHeight, int paddingX, int paddingY) {
        return new Rect(paddingX, paddingY, paddingX + dataWidth, paddingY + dataHeight);
    }

    private Rect layoutFitEnd(int dataWidth, int dataHeight, int displayWidth, int displayHeight, int paddingX, int paddingY) {
        return new Rect(displayWidth - paddingX - dataWidth, displayHeight - paddingY - dataHeight, displayWidth - paddingX, displayHeight - paddingY);
    }

    private Rect layoutFill(int displayWidth, int displayHeight) {
        return new Rect(0, 0, displayWidth, displayHeight);
    }
}