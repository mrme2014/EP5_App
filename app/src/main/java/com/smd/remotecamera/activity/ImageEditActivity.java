package com.smd.remotecamera.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.smd.remotecamera.R;
import com.smd.remotecamera.constants.FileConstants;
import com.smd.remotecamera.fragment.ImageEditBottomFragment;
import com.smd.remotecamera.util.CommonUtil;
import com.smd.remotecamera.util.ImageUtil;
import com.smd.remotecamera.util.Util;
import com.smd.remotecamera.view.DragLayout;

import java.io.IOException;

import HaoRan.ImageFilter.IImageFilter;
import HaoRan.ImageFilter.Image;


public class ImageEditActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener,
        DragLayout.OnAddTextViewListener, View.OnClickListener {

    public static final String KEY_TYPE = "type";
    public static final String KEY_IMGPATH = "imagePath";
    public static final int SHUI = 1;
    public static final int TEXT = 2;
    public static final int FILTER = 3;

    private ImageButton mIbBack;
    private ImageButton mIbOk;
    private FrameLayout mContainer;
    private RadioGroup mRgMenu;
    private RadioButton mRb1;
    private RadioButton mRb2;
    private RadioButton mRb3;
    private RadioButton mRb4;
    private ImageView mIvMain;
    private FrameLayout mPG;
    private EditText mEtInvisible;
    private DragLayout mDragLayout;

    private FragmentManager mFragmentManager;
    private ImageEditBottomFragment mBottomFragment;

    private String mImgPath;
    private IImageFilter mIImageFilter;

    private Bitmap mBitmap;

    private Bitmap mTmpBitmap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageedit);

        mImgPath = getIntent().getStringExtra(KEY_IMGPATH);

        initView();
        init();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap resBitmap = BitmapFactory.decodeFile(mImgPath);
                Matrix matrix = new Matrix();
                matrix.setScale(0.2f, 0.2f);
                mBitmap = Bitmap.createBitmap(resBitmap, 0, 0, resBitmap.getWidth(),
                        resBitmap.getHeight(), matrix, true);
                mTmpBitmap = mBitmap;
                mDragLayout.setBmpSize(mBitmap.getWidth(), mBitmap.getHeight());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mIvMain.setImageBitmap(mBitmap);
                    }
                });
                resBitmap.recycle();
                System.gc();
                if (mIImageFilter != null) {
                    mOnTypeChangeListener.onFilterChanged(mIImageFilter);
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        if (mBitmap != null) {
            mBitmap.recycle();
            mTmpBitmap.recycle();
            System.gc();
        }
        super.onDestroy();
    }

    private void initView() {
        mIbBack = (ImageButton) findViewById(R.id.activity_imageedit_ib_back);
        mIbOk = (ImageButton) findViewById(R.id.activity_imageedit_ib_ok);
        mContainer = (FrameLayout) findViewById(R.id.activity_imageedit_bottom_container);
        mRgMenu = (RadioGroup) findViewById(R.id.activity_imageedit_rg);
        mRb1 = (RadioButton) findViewById(R.id.activity_imageedit_rb_1);
        mRb2 = (RadioButton) findViewById(R.id.activity_imageedit_rb_2);
        mRb3 = (RadioButton) findViewById(R.id.activity_imageedit_rb_3);
        mRb4 = (RadioButton) findViewById(R.id.activity_imageedit_rb_4);
        mIvMain = (ImageView) findViewById(R.id.activity_imageedit_iv);
        mPG = (FrameLayout) findViewById(R.id.activity_imageedit_pg);
        mEtInvisible = (EditText) findViewById(R.id.activity_imageedit_et);
        mDragLayout = (DragLayout) findViewById(R.id.activity_imageedit_drag);
        mEtInvisible.addTextChangedListener(mTextWatcher);
        mDragLayout.setOnAddTextViewListener(this);
        mRgMenu.setOnCheckedChangeListener(this);
        mIbBack.setOnClickListener(this);
        mIbOk.setOnClickListener(this);
        mIvMain.setImageResource(R.drawable.timg);
    }

    private void init() {
        if (mBottomFragment == null) {
            mBottomFragment = new ImageEditBottomFragment();
            mBottomFragment.setOnTypeChangeListener(mOnTypeChangeListener);
        }
        mFragmentManager = getSupportFragmentManager();
        switch (getIntent().getIntExtra(KEY_TYPE, SHUI)) {
            case SHUI:
                mRb2.setChecked(true);
                break;
            case TEXT:
                mRb3.setChecked(true);
                break;
            case FILTER:
                mRb4.setChecked(true);
                break;
        }
        mBottomFragment.setType(getIntent().getIntExtra(KEY_TYPE, SHUI));
        FragmentTransaction shuiTransaction = mFragmentManager.beginTransaction();
        shuiTransaction.add(R.id.activity_imageedit_bottom_container, mBottomFragment);
        shuiTransaction.commit();
    }

    private void showSoftInput() {
        mEtInvisible.setFocusable(true);
        mEtInvisible.setFocusableInTouchMode(true);
        mEtInvisible.requestFocus();
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(mEtInvisible, 0);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        switch (checkedId) {
            case R.id.activity_imageedit_rb_1:
                mDragLayout.reset();
                mIvMain.setImageBitmap(mBitmap);
                break;
            case R.id.activity_imageedit_rb_2:
                mBottomFragment.setType(SHUI);
                break;
            case R.id.activity_imageedit_rb_3:
                mDragLayout.setShouldAddView(true);
                mBottomFragment.setType(TEXT);
                break;
            case R.id.activity_imageedit_rb_4:
                mBottomFragment.setType(FILTER);
                break;
        }
    }

    @Override
    public void onAddTextView() {
        showSoftInput();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_imageedit_ib_back:

                break;
            case R.id.activity_imageedit_ib_ok:
                save();
                mBitmap.recycle();
                CommonUtil.showToast(getApplicationContext(), "保存成功");
                finish();
                break;
        }
    }

    private void save() {
        try {
            if (mDragLayout.getText() != null) {
                float scale = mDragLayout.getScale();
                mTmpBitmap = ImageUtil.drawTextToBitmap(this, mTmpBitmap, mDragLayout.getText(), mDragLayout.getTextSize() / scale,
                        mDragLayout.getFont(), mDragLayout.getXY().first / scale, mDragLayout.getXY().second / scale);
            }
            Util.saveToFile(FileConstants.LOCAL_EDIT_PATH, createName(), mTmpBitmap, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String createName() {
        String newName = mImgPath.substring(mImgPath.lastIndexOf("/") + 1);
        newName = newName.substring(0, newName.lastIndexOf('.')) + System.currentTimeMillis() + FileConstants.POSTFIX_PHOTO_EDIT;
        return newName;
    }

    private class ProcessImageTask extends AsyncTask<Void, Void, Bitmap> {

        private IImageFilter filter;

        public ProcessImageTask(IImageFilter imageFilter) {
            this.filter = imageFilter;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        public Bitmap doInBackground(Void... params) {
            Image img = null;
            try {
                img = new Image(mBitmap);
                if (filter != null) {
                    img = filter.process(img);
                    img.copyPixelsFromBuffer();
                }
                return img.getImage();
            } catch (Exception e) {
                if (img != null && img.destImage.isRecycled()) {
                    img.destImage.recycle();
                    img.destImage = null;
                    System.gc(); // 提醒系统及时回收
                }
            } finally {
                if (img != null && img.image.isRecycled()) {
                    img.image.recycle();
                    img.image = null;
                    System.gc(); // 提醒系统及时回收
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            mTmpBitmap = result;
            if (result != null) {
                super.onPostExecute(result);
                mIvMain.setImageBitmap(result);
                mPG.setVisibility(View.GONE);
            }
        }
    }

    private boolean checkResBitmapInit() {
        if (mBitmap == null) {
            return false;
        }
        return true;
    }

    private ImageEditBottomFragment.OnTypeChangeListener mOnTypeChangeListener = new ImageEditBottomFragment.OnTypeChangeListener() {
        @Override
        public void onFilterChanged(IImageFilter filter) {
            mPG.setVisibility(View.VISIBLE);
            if (!checkResBitmapInit()) {
                if (filter == null) {
                    mPG.setVisibility(View.GONE);
                    return;
                }
                mIImageFilter = filter;
                return;
            }
            if (filter == null) {
                mIvMain.setImageBitmap(mBitmap);
                mPG.setVisibility(View.GONE);
                return;
            }
            new ProcessImageTask(filter).execute();
        }

        @Override
        public void onShuiChanged(int resId) {

        }

        @Override
        public void onFontChanged(String font) {
            mDragLayout.setFont(font);
        }
    };

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mDragLayout.setText(s.toString());
        }
    };


}
