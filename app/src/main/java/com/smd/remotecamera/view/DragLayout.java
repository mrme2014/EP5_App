package com.smd.remotecamera.view;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.v4.util.Pair;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.qiaomu.libvideo.utils.AppUtils;
import com.smd.remotecamera.R;
import com.smd.remotecamera.util.Util;


public class DragLayout extends FrameLayout {

    private ViewDragHelper mViewDragHelper;

    private boolean mShouldAddView = false;
    private boolean mAskDelete = true;
    private TextView mTextView;
    private int mTextX;
    private int mTextY;

    private int mDownX;
    private int mDownY;

    private int mBmpWidth;
    private int mBmpHeight;
    private int mImgWidth;
    private int mImgHeight;

    private OnAddTextViewListener mOnAddTextViewListener;

    public DragLayout(Context context) {
        this(context, null);
    }

    public DragLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mViewDragHelper = ViewDragHelper.create(this, 1.0f, mCallback);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mTextView != null) {
            mTextView.layout(mTextX, mTextY, mTextX + mTextView.getMeasuredWidth(), mTextY + mTextView.getMeasuredHeight());
        }
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mShouldAddView && event.getAction() == MotionEvent.ACTION_UP) {
            addTextView(event.getX(), event.getY());
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mDownX = (int) event.getX();
            mDownY = (int) event.getY();
        }
        if ((mTextView != null) && (event.getAction() == MotionEvent.ACTION_UP) && (Math.abs(event.getX() - mDownX) < 1) && (Math.abs(event.getY() - mDownY) < 1) && (mOnAddTextViewListener != null)) {
            int[] postions = new int[2];
            mTextView.getLocationOnScreen(postions);
            RectF rectF = new RectF(postions[0], postions[1], postions[0] + mTextView.getWidth(), postions[1] + mTextView.getHeight());
            if (!rectF.contains(event.getRawX(), event.getRawY()))
                mOnAddTextViewListener.onAddTextView();
        }
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    public void setShouldAddView(boolean shouldAdd) {
        mShouldAddView = shouldAdd;
    }

    public void setText(String str) {
        if (mTextView != null) mTextView.setText(str);
    }

    public void setFont(String font) {
        if (mTextView == null) {
            return;
        }
        Typeface mtypeface = Typeface.createFromAsset(getContext().getAssets(), font);
        mTextView.setTypeface(mtypeface);
    }

    public String getText() {
        return mTextView == null ? null : mTextView.getText().toString();
    }

    public float getTextSize() {
        return mTextView == null ? 0 : mTextView.getTextSize();
    }

    public Typeface getFont() {
        return mTextView == null ? null : mTextView.getTypeface();
    }

    public float getScale() {
        return 1.0f * mImgWidth / mBmpWidth;
    }

    public Pair<Integer, Integer> getXY() {
        int[] location = new int[2];
        getLocationOnScreen(location);
        Paint.FontMetricsInt fontMetrics = mTextView.getPaint().getFontMetricsInt();
        return new Pair<>(mTextView.getLeft(), mTextView.getTop() - location[1] - fontMetrics.bottom);
    }

    public void setBmpSize(int width, int height) {
        mBmpWidth = width;
        mBmpHeight = height;
        android.util.Pair<Integer, Integer> screenSize = Util.getScreenSize(getContext());
        mImgWidth = screenSize.first;
        mImgHeight = (int) (1.0 * mBmpHeight / mBmpWidth * mImgWidth);
    }

    public void reset() {
        removeAllViews();
        mTextView = null;
    }

    private void addTextView(float x, float y) {
        mShouldAddView = false;
        mTextX = (int) x;
        mTextY = (int) y;
        final TextView textView = new TextView(getContext());
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(layoutParams);
        addView(textView);
        mTextView = textView;
        textView.setTextColor(Color.WHITE);
        requestLayout();
        if (mOnAddTextViewListener != null) {
            mOnAddTextViewListener.onAddTextView();
        }
    }

    private ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            boolean capture = child != DragLayout.this;
            mAskDelete = capture;
            return capture;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (left > mImgWidth || left < 0) {
                return mTextX;
            }
            mTextX = left;
            return left;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            if (top < (getMeasuredHeight() - mImgHeight) / 2) {
                return mTextY;
            } else if (top > mImgHeight + (getMeasuredHeight() - mImgHeight) / 2 - mTextView.getMeasuredHeight()) {
                return mTextY;
            }
            mTextY = top;
            return top;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            mAskDelete = false;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            if (!mAskDelete)
                return;
            AppUtils.showAlertDialog(getContext(), R.string.tip_delete_text, R.string.ok, R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (which == -1) {
                        removeView(mTextView);
                    }
                }
            });
            mAskDelete = true;
        }
    };

    public void setOnAddTextViewListener(OnAddTextViewListener onAddTextViewListener) {
        mOnAddTextViewListener = onAddTextViewListener;
    }


    public interface OnAddTextViewListener {
        void onAddTextView();
    }

}
