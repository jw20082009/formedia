package com.wantee.formedia.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class PressLinearLayout extends LinearLayout {

    public PressLinearLayout(Context context) {
        super(context);
    }

    public PressLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PressLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PressLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (isPressed() || isSelected()) {
            setAlpha(0.6f);
        } else {
            setAlpha(1.0f);
        }
    }
}
