package com.smd.remotecamera.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.smd.remotecamera.util.CommonUtil;

public class SquareImageView extends AppCompatImageView {

        private float mScale;

        public SquareImageView(Context context) {
                super(context);
        }

        public SquareImageView(Context context, AttributeSet attrs) {
                super(context, attrs);
        }

        public SquareImageView(Context context, AttributeSet attrs, int defStyleAttr) {
                super(context, attrs, defStyleAttr);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int width = MeasureSpec.getSize(widthMeasureSpec);
                int widthMode = MeasureSpec.getMode(widthMeasureSpec);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) (width * mScale), widthMode);
                CommonUtil.SYSO("testSizeSize", "" + (int) (width * mScale));
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

        @Override
        protected void onDraw(Canvas canvas) {
                Drawable drawable = getDrawable();
                CommonUtil.SYSO("testSizeSize", "" + drawable.getIntrinsicWidth() + "; " + drawable.getIntrinsicHeight() + "; " + getWidth() + "; " + getHeight());
                if (Math.abs((1.0F * drawable.getIntrinsicWidth() / drawable.getIntrinsicHeight()) - 1.0F * getWidth() / getHeight()) > 0.01) {
                        mScale = 1.0F * drawable.getIntrinsicHeight() / drawable.getIntrinsicWidth();
                        requestLayout();
                } else {
                        super.onDraw(canvas);
                }
        }

        /**
         * @param scale 图片高/宽
         */
        private void setScale(float scale) {
                mScale = scale;
                if (Math.abs(scale - mScale) < 0.0001) {
                        return;
                }
                invalidate();
        }

        private float getScale() {
                return mScale;
        }
}
