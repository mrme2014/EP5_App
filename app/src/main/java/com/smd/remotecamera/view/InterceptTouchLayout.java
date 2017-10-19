package com.smd.remotecamera.view;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;


public class InterceptTouchLayout extends FrameLayout {
        public InterceptTouchLayout(@NonNull Context context) {
                super(context);
        }

        public InterceptTouchLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
                super(context, attrs);
        }

        public InterceptTouchLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
                super(context, attrs, defStyleAttr);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
                return true;
        }
}
