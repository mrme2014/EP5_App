package com.smd.remotecamera.fragment;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.smd.remotecamera.R;
import com.smd.remotecamera.activity.ImageEditActivity;
import com.smd.remotecamera.constants.FileConstants;

import HaoRan.ImageFilter.BlackWhiteFilter;
import HaoRan.ImageFilter.ColorToneFilter;
import HaoRan.ImageFilter.EdgeFilter;
import HaoRan.ImageFilter.Gradient;
import HaoRan.ImageFilter.IImageFilter;
import HaoRan.ImageFilter.PaintBorderFilter;
import HaoRan.ImageFilter.SceneFilter;
import HaoRan.ImageFilter.SharpFilter;
import HaoRan.ImageFilter.YCBCrLinearFilter;


public class ImageEditBottomFragment extends Fragment implements View.OnClickListener {

    private HorizontalScrollView mHsvShui;
    private HorizontalScrollView mHsvFilter;
    private LinearLayout mLlText;
    private RelativeLayout mFilter1;
    private RelativeLayout mFilter2;
    private RelativeLayout mFilter3;
    private RelativeLayout mFilter4;
    private RelativeLayout mFilter5;
    private RelativeLayout mFilter6;
    private RelativeLayout mFilter7;
    private RelativeLayout mFilter8;
    private ImageView mCircle1;
    private ImageView mCircle2;
    private ImageView mCircle3;
    private ImageView mCircle4;
    private ImageView mCircle5;
    private ImageView mCircle6;
    private ImageView mCircle7;
    private ImageView mCircle8;
    private Button mBtnType;

    private ImageView mLastCircle;
    private ImageView mCurrCircle;

    private int mType = ImageEditActivity.SHUI;

    private OnTypeChangeListener mOnTypeChangeListener;

    private static final IImageFilter[] FILTER_DATA = new IImageFilter[]{
            null,
            new BlackWhiteFilter(),
            new SceneFilter(5f, Gradient.Scene()),
            new EdgeFilter(),
            new ColorToneFilter(Color.rgb(33, 168, 254), 192),
            new PaintBorderFilter(0x00FFFF),
            new YCBCrLinearFilter(new YCBCrLinearFilter.Range(
                    -0.276f, 0.163f),
                    new YCBCrLinearFilter.Range(-0.202f, 0.5f)),
            new SharpFilter()
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_imageedit, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mHsvShui = (HorizontalScrollView) view.findViewById(R.id.fragment_imageedit_hsv_shui);
        mHsvFilter = (HorizontalScrollView) view.findViewById(R.id.fragment_imageedit_hsv_filter);
        mLlText = (LinearLayout) view.findViewById(R.id.fragment_imageedit_ll_text);
        mFilter1 = (RelativeLayout) view.findViewById(R.id.fragment_imageedit_filter_1);
        mFilter2 = (RelativeLayout) view.findViewById(R.id.fragment_imageedit_filter_2);
        mFilter3 = (RelativeLayout) view.findViewById(R.id.fragment_imageedit_filter_3);
        mFilter4 = (RelativeLayout) view.findViewById(R.id.fragment_imageedit_filter_4);
        mFilter5 = (RelativeLayout) view.findViewById(R.id.fragment_imageedit_filter_5);
        mFilter6 = (RelativeLayout) view.findViewById(R.id.fragment_imageedit_filter_6);
        mFilter7 = (RelativeLayout) view.findViewById(R.id.fragment_imageedit_filter_7);
        mFilter8 = (RelativeLayout) view.findViewById(R.id.fragment_imageedit_filter_8);
        mCircle1 = (ImageView) view.findViewById(R.id.fragment_imageedit_filter_select_1);
        mCircle2 = (ImageView) view.findViewById(R.id.fragment_imageedit_filter_select_2);
        mCircle3 = (ImageView) view.findViewById(R.id.fragment_imageedit_filter_select_3);
        mCircle4 = (ImageView) view.findViewById(R.id.fragment_imageedit_filter_select_4);
        mCircle5 = (ImageView) view.findViewById(R.id.fragment_imageedit_filter_select_5);
        mCircle6 = (ImageView) view.findViewById(R.id.fragment_imageedit_filter_select_6);
        mCircle7 = (ImageView) view.findViewById(R.id.fragment_imageedit_filter_select_7);
        mCircle8 = (ImageView) view.findViewById(R.id.fragment_imageedit_filter_select_8);
        mBtnType = (Button) view.findViewById(R.id.fragment_imageedit_text_font);
        mFilter1.setOnClickListener(this);
        mFilter2.setOnClickListener(this);
        mFilter3.setOnClickListener(this);
        mFilter4.setOnClickListener(this);
        mFilter5.setOnClickListener(this);
        mFilter6.setOnClickListener(this);
        mFilter7.setOnClickListener(this);
        mFilter8.setOnClickListener(this);
        mBtnType.setOnClickListener(this);
        changeType();
        mLastCircle = mCircle1;
        mCurrCircle = mCircle1;
    }

    public void setType(int type) {
        mType = type;
        changeType();
    }

    private void changeType() {
        if (mHsvShui == null) {
            return;
        }
        switch (mType) {
            case ImageEditActivity.SHUI:
                mHsvShui.setVisibility(View.VISIBLE);
                mHsvFilter.setVisibility(View.GONE);
                mLlText.setVisibility(View.GONE);
                break;
            case ImageEditActivity.FILTER:
                mHsvShui.setVisibility(View.GONE);
                mHsvFilter.setVisibility(View.VISIBLE);
                mLlText.setVisibility(View.GONE);
                break;
            case ImageEditActivity.TEXT:
                mHsvShui.setVisibility(View.GONE);
                mHsvFilter.setVisibility(View.GONE);
                mLlText.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        IImageFilter filter = null;
        switch (v.getId()) {
            case R.id.fragment_imageedit_filter_1:
                mLastCircle.setVisibility(View.INVISIBLE);
                mCircle1.setVisibility(View.VISIBLE);
                mLastCircle = mCircle1;
                filter = FILTER_DATA[0];
                break;
            case R.id.fragment_imageedit_filter_2:
                mLastCircle.setVisibility(View.INVISIBLE);
                mCircle2.setVisibility(View.VISIBLE);
                mLastCircle = mCircle2;
                filter = FILTER_DATA[1];
                break;
            case R.id.fragment_imageedit_filter_3:
                mLastCircle.setVisibility(View.INVISIBLE);
                mCircle3.setVisibility(View.VISIBLE);
                mLastCircle = mCircle3;
                filter = FILTER_DATA[2];
                break;
            case R.id.fragment_imageedit_filter_4:
                mLastCircle.setVisibility(View.INVISIBLE);
                mCircle4.setVisibility(View.VISIBLE);
                mLastCircle = mCircle4;
                filter = FILTER_DATA[3];
                break;
            case R.id.fragment_imageedit_filter_5:
                mLastCircle.setVisibility(View.INVISIBLE);
                mCircle5.setVisibility(View.VISIBLE);
                mLastCircle = mCircle5;
                filter = FILTER_DATA[4];
                break;
            case R.id.fragment_imageedit_filter_6:
                mLastCircle.setVisibility(View.INVISIBLE);
                mCircle6.setVisibility(View.VISIBLE);
                mLastCircle = mCircle6;
                filter = FILTER_DATA[5];
                break;
            case R.id.fragment_imageedit_filter_7:
                mLastCircle.setVisibility(View.INVISIBLE);
                mCircle7.setVisibility(View.VISIBLE);
                mLastCircle = mCircle7;
                filter = FILTER_DATA[6];
                break;
            case R.id.fragment_imageedit_filter_8:
                mLastCircle.setVisibility(View.INVISIBLE);
                mCircle8.setVisibility(View.VISIBLE);
                mLastCircle = mCircle8;
                filter = FILTER_DATA[7];
                break;
            case R.id.fragment_imageedit_text_font:
                showListDialog();
                break;
        }
        if (mOnTypeChangeListener != null) {
            if (mType == ImageEditActivity.FILTER) {
                mOnTypeChangeListener.onFilterChanged(filter);
            }
        }
    }

    private void showListDialog() {
        final String[] items = {"黑体", "华文隶书", "华文宋体", "华文行楷"};
        AlertDialog.Builder listDialog = new AlertDialog.Builder(getContext());
        listDialog.setTitle("字体选择");
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mOnTypeChangeListener != null) {
                    String font = null;
                    switch (which) {
                        case 0:
                            font = FileConstants.FONT_HEITI;
                            break;
                        case 1:
                            font = FileConstants.FONT_HUAWENLLISHU;
                            break;
                        case 2:
                            font = FileConstants.FONT_HUAWENSONGTI;
                            break;
                        case 3:
                            font = FileConstants.FONT_HUAWENXINGKAI;
                            break;
                    }
                    mOnTypeChangeListener.onFontChanged(font);
                }
            }
        });
        listDialog.show();
    }

    public void setOnTypeChangeListener(OnTypeChangeListener onTypeChangeListener) {
        mOnTypeChangeListener = onTypeChangeListener;
    }

    public interface OnTypeChangeListener {
        void onFilterChanged(IImageFilter filter);

        void onShuiChanged(int resId);

        void onFontChanged(String font);
    }

}
