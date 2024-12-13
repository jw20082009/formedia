package com.wantee.formedia.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PressTextview extends androidx.appcompat.widget.AppCompatTextView {


    public PressTextview(@NonNull Context context) {
        super(context);
    }

    public PressTextview(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PressTextview(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
