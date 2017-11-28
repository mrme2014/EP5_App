package com.smd.remotecamera.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qiaomu.libvideo.utils.AppUtils;
import com.qiaomu.libvideo.utils.ToastUtils;
import com.smd.remotecamera.R;
import com.smd.remotecamera.constants.FileConstants;
import com.smd.remotecamera.fragment.ImageEditBottomFragment;
import com.smd.remotecamera.util.CommonUtil;
import com.smd.remotecamera.util.TuyaUtils;
import com.smd.remotecamera.util.Util;
import com.smd.remotecamera.view.DragLayout;
import com.smd.remotecamera.view.TouchView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;

import HaoRan.ImageFilter.IImageFilter;
import HaoRan.ImageFilter.Image;
import jczj.android.com.sharelib.ShareDialog;


public class ImageEditActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener,
        DragLayout.OnAddTextViewListener, View.OnClickListener {

    public static final String KEY_TYPE = "type";
    public static final String KEY_IMGPATH = "imagePath";
    public static final int SHUI = 1;
    public static final int TEXT = 2;
    public static final int FILTER = 3;

    private ImageView mIbBack;
    private ImageButton mIbOk;
    private FrameLayout mContainer;
    private RadioGroup mRgMenu;
    private RadioButton mRb1;
    private RadioButton mRb2;
    private RadioButton mRb3;
    private RadioButton mRb4;
    private ImageView mIvMain;
    private FrameLayout mPG;
    // private EditText mEtInvisible;
    private DragLayout mDragLayout;
    private RelativeLayout waterLayout;
    private RelativeLayout rl_edit_text;
    private EditText et_tag;
    private TextView tv_tag;
    private TextView tv_finish;
    private TextView tv_close;

    private FrameLayout cacheLayout;

    private FragmentManager mFragmentManager;
    private ImageEditBottomFragment mBottomFragment;

    private String mImgPath;
    private IImageFilter mIImageFilter;

    private Bitmap mBitmap;

    private Bitmap mTmpBitmap;
    private String edit_path;
    private InputMethodManager manager;
    private int windowWidth;
    private int windowHeight;
    private int dp100;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageedit);
        manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        windowWidth = getWindowManager().getDefaultDisplay().getWidth();
        windowHeight = getWindowManager().getDefaultDisplay().getHeight();
        dp100 = (int) getResources().getDimension(R.dimen.dp100);


        mImgPath = getIntent().getStringExtra(KEY_IMGPATH);

        initView();
        init();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
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
                            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(mIvMain.getWidth(), 2 * mTmpBitmap.getHeight());
                            mDragLayout.setLayoutParams(params);
                            waterLayout.setLayoutParams(params);
                        }
                    });
                    resBitmap.recycle();
                    System.gc();
                    if (mIImageFilter != null) {
                        mOnTypeChangeListener.onFilterChanged(mIImageFilter);
                    }
                } catch (Exception e) {
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
        tv_tag = (TextView) findViewById(R.id.tv_tag);
        rl_edit_text = (RelativeLayout) findViewById(R.id.rl_edit_text);
        et_tag = (EditText) findViewById(R.id.et_tag);
        tv_finish = (TextView) findViewById(R.id.tv_finish);
        tv_close = (TextView) findViewById(R.id.tv_close);

        waterLayout = (RelativeLayout) findViewById(R.id.waterLayout);
        cacheLayout = (FrameLayout) findViewById(R.id.cacheLayout);
        mIbBack = (ImageView) findViewById(R.id.activity_imageedit_ib_back);
        mIbOk = (ImageButton) findViewById(R.id.activity_imageedit_ib_ok);
        mContainer = (FrameLayout) findViewById(R.id.activity_imageedit_bottom_container);
        mRgMenu = (RadioGroup) findViewById(R.id.activity_imageedit_rg);
        mRb1 = (RadioButton) findViewById(R.id.activity_imageedit_rb_1);
        mRb2 = (RadioButton) findViewById(R.id.activity_imageedit_rb_2);
        mRb3 = (RadioButton) findViewById(R.id.activity_imageedit_rb_3);
        mRb4 = (RadioButton) findViewById(R.id.activity_imageedit_rb_4);
        mIvMain = (ImageView) findViewById(R.id.activity_imageedit_iv);
        mPG = (FrameLayout) findViewById(R.id.activity_imageedit_pg);
        // mEtInvisible = (EditText) findViewById(R.id.activity_imageedit_et);
        mDragLayout = (DragLayout) findViewById(R.id.activity_imageedit_drag);
        et_tag.addTextChangedListener(mTextWatcher);
        mDragLayout.setOnAddTextViewListener(this);
        mRgMenu.setOnCheckedChangeListener(this);
        mIbBack.setOnClickListener(this);
        mIbOk.setOnClickListener(this);
        tv_finish.setOnClickListener(this);
        tv_close.setOnClickListener(this);
        mIvMain.setImageResource(R.drawable.timg);

        waterLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRb3.isChecked())
                    changeTextState(true);
            }
        });
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
        if (!mRb3.isChecked())
            return;
        et_tag.setFocusable(true);
        et_tag.setFocusableInTouchMode(true);
        et_tag.requestFocus();
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(et_tag, 0);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        switch (checkedId) {
            case R.id.activity_imageedit_rb_1:
                mDragLayout.reset();
                waterLayout.removeAllViews();
                mIvMain.setImageBitmap(mBitmap);
                break;
            case R.id.activity_imageedit_rb_2:
                mBottomFragment.setType(SHUI);
                break;
            case R.id.activity_imageedit_rb_3:
                //mDragLayout.setShouldAddView(true);
                changeTextState(!(rl_edit_text.getVisibility() == View.VISIBLE));
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
                this.finish();
                break;
            case R.id.activity_imageedit_ib_ok:
                save();
                CommonUtil.showToast(getApplicationContext(), "保存成功");
                break;
            case R.id.tv_close:
                changeTextState(!(rl_edit_text.getVisibility() == View.VISIBLE));
                break;
            case R.id.tv_finish:
                changeTextState(!(rl_edit_text.getVisibility() == View.VISIBLE));
                if (et_tag.getText().length() > 0) {
                    TuyaUtils.addTextToWindow(this, waterLayout, tv_tag);
                    et_tag.setText("");
                }
        }
    }

    private void save() {
        try {

            cacheLayout.setDrawingCacheEnabled(true);
            Bitmap drawingCache = cacheLayout.getDrawingCache();
            String filename = createName();
            Util.saveToFile(FileConstants.LOCAL_EDIT_PATH, filename, drawingCache, true);
            cacheLayout.setDrawingCacheEnabled(false);
            edit_path = FileConstants.LOCAL_EDIT_PATH + "/" + filename;
            AppUtils.insertImage(this, edit_path);
            ShareDialog.newInstance(edit_path).show(getSupportFragmentManager());
            Toast.makeText(getApplicationContext(), "图片已保存到/sdcard/C-Video/Edit目录下", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String createName() {
        String newName = mImgPath.substring(mImgPath.lastIndexOf("/") + 1);
        newName = "0" + newName.substring(0, newName.lastIndexOf('.')) + FileConstants.POSTFIX_PHOTO_EDIT;
        File file = new File(FileConstants.LOCAL_EDIT_PATH, newName);
        if (file.exists())
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
            TuyaUtils.addWater(ImageEditActivity.this, waterLayout, resId);
        }

        @Override
        public void onFontChanged(int type, int position, String font) {
            if (type == 0) {
                mDragLayout.setFont(font);
            } else if (type == 1) {
                et_tag.setTextSize(Float.parseFloat(font.substring(0, 2)));
            } else {
                if (position == 0) {
                    tv_tag.setTextColor(Color.WHITE);
                    et_tag.setTextColor(Color.WHITE);
                } else if (position == 1) {
                    et_tag.setTextColor(Color.BLACK);
                    tv_tag.setTextColor(Color.BLACK);
                } else if (position == 2) {
                    et_tag.setTextColor(Color.RED);
                    tv_tag.setTextColor(Color.RED);
                } else if (position == 3) {
                    et_tag.setTextColor(Color.GREEN);
                    tv_tag.setTextColor(Color.GREEN);
                } else if (position == 4) {
                    et_tag.setTextColor(Color.YELLOW);
                    tv_tag.setTextColor(Color.YELLOW);
                }
            }
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
            tv_tag.setText(s);
        }
    };


    private void changeTextState(boolean flag) {

        if (flag) {
            rl_edit_text.setY(windowHeight);
            rl_edit_text.setVisibility(View.VISIBLE);
            startAnim(rl_edit_text.getY(), 0, null);
            popupEditText();
        } else {
            manager.hideSoftInputFromWindow(et_tag.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            startAnim(0, windowHeight, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    rl_edit_text.setVisibility(View.GONE);
                }
            });
        }
    }

    boolean isFirstShowEditText;

    public void popupEditText() {
        isFirstShowEditText = true;
        et_tag.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (isFirstShowEditText) {
                    isFirstShowEditText = false;
                    et_tag.setFocusable(true);
                    et_tag.setFocusableInTouchMode(true);
                    et_tag.requestFocus();
                    isFirstShowEditText = !manager.showSoftInput(et_tag, 0);
                }
            }
        });
    }

    /**
     * 执行文字编辑区域动画
     */
    private void startAnim(float start, float end, AnimatorListenerAdapter listenerAdapter) {

        ValueAnimator va = ValueAnimator.ofFloat(start, end).setDuration(200);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                rl_edit_text.setY(value);
            }
        });
        if (listenerAdapter != null) {
            va.addListener(listenerAdapter);
        }
        va.start();
    }

}
