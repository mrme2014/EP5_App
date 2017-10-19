package com.smd.remotecamera.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smd.remotecamera.R;
import com.smd.remotecamera.adapter.FileListAdapter;
import com.smd.remotecamera.bean.RemoteFileBean;
import com.smd.remotecamera.constants.FileConstants;
import com.smd.remotecamera.core.DownloadCore;
import com.smd.remotecamera.fragment.RemoteFileListFragment;
import com.smd.remotecamera.util.Util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;


public class EditListActivity extends AppCompatActivity implements FileListAdapter.OnCheckedNumChangedListener, View.OnClickListener {

    private LinearLayout mLLVideo;
    private LinearLayout mLLPhoto;
    private TextView mTvCut;
    private TextView mTvCompress;
    private TextView mTvMov;
    private TextView mTvMp4;
    private ImageButton mIbShui;
    private TextView mTvText;
    private TextView mTvFilter;

    private RemoteFileListFragment mRemoteFileListFragment;

//    private RemoteFileController mRemoteFileController;

    private List<RemoteFileBean> mVideoList;
    private List<RemoteFileBean> mPhotoList;
    private List<RemoteFileBean> mCheckedList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editlist);

        initView();
        init();

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                final String ack = NVTKitModel.changeMode(NVTKitModel.MODE_PLAYBACK);
//                mRemoteFileController.qeryRemoteFileList();
//            }
//        }).start();
    }

    @Override
    protected void onResume() {
        loadData();
        super.onResume();
    }

    @Override
    public void onBackPressed() {
//        if (mDownloadBinder.checkHasDownloadFile()) {
//            showBackDialog();
//            return;
//        }
        finish();
    }

    private void initView() {
        mLLVideo = (LinearLayout) findViewById(R.id.activity_editfilelist_bottom_video);
        mLLPhoto = (LinearLayout) findViewById(R.id.activity_editfilelist_bottom_photo);
        mTvCut = (TextView) findViewById(R.id.activity_editfilelist_tv_cut);
        mTvCompress = (TextView) findViewById(R.id.activity_editfilelist_tv_compress);
        mTvMov = (TextView) findViewById(R.id.activity_editfilelist_tv_mov);
        mTvMp4 = (TextView) findViewById(R.id.activity_editfilelist_tv_mp4);
        mIbShui = (ImageButton) findViewById(R.id.activity_editfilelist_ib_shui);
        mTvText = (TextView) findViewById(R.id.activity_editfilelist_tv_text);
        mTvFilter = (TextView) findViewById(R.id.activity_editfilelist_tv_filter);
        mTvCut.setOnClickListener(this);
        mTvCompress.setOnClickListener(this);
        mTvMov.setOnClickListener(this);
        mTvMp4.setOnClickListener(this);
        mIbShui.setOnClickListener(this);
        mTvText.setOnClickListener(this);
        mTvFilter.setOnClickListener(this);
    }

    private void init() {
//        mRemoteFileController = new RemoteFileController();
//        mRemoteFileController.setOnRemoteFileQueryFinishListener(mOnQueryFinishListener);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        mRemoteFileListFragment = new RemoteFileListFragment(true);
        mRemoteFileListFragment.addOnPageChangeListener(mOnPageChangeListener);
        mRemoteFileListFragment.setOnClickBackListener(mOnClickBackListener);
        mRemoteFileListFragment.setOnCheckedNumChangedListener(this);
        transaction.add(R.id.activity_editfilelist_container, mRemoteFileListFragment);
        transaction.commit();
    }

    private void loadData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File videoFolder = new File(FileConstants.LOCAL_VIDEO_PATH);
                RemoteFileBean tmpBean;
                if (videoFolder.exists()) {
                    File[] videoFiles = videoFolder.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            if (name.endsWith(FileConstants.POSTFIX_VIDEO)) {
                                return true;
                            }
                            return false;
                        }
                    });
                    mVideoList = new ArrayList<RemoteFileBean>();
                    for (File file : videoFiles) {
                        tmpBean = new RemoteFileBean(file.getName(), null, file.length(), Util.getTimeStr(file.getName()), true);
                        mVideoList.add(tmpBean);
                    }
                }
                File photoFolder = new File(FileConstants.LOCAL_PHOTO_PATH);
                if (photoFolder.exists()) {
                    File[] photoFiles = photoFolder.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            if (name.endsWith(FileConstants.POSTFIX_PHOTO)) {
                                return true;
                            }
                            return false;
                        }
                    });
                    mPhotoList = new ArrayList<RemoteFileBean>();
                    for (File file : photoFiles) {
                        tmpBean = new RemoteFileBean(file.getName(), null, file.length(), Util.getTimeStr(file.getName()), true);
                        mPhotoList.add(tmpBean);
                    }
                }
                mRemoteFileListFragment.setData(mVideoList, mPhotoList);
            }
        }).start();
    }

//    private RemoteFileController.OnRemoteFileQueryFinishListener mOnQueryFinishListener = new RemoteFileController.OnRemoteFileQueryFinishListener() {
//        @Override
//        public void onQueryFinished(List<RemoteFileBean> videoData, List<RemoteFileBean> photoData) {
//            mVideoList = videoData;
//            mPhotoList = photoData;
//            mRemoteFileListFragment.setData(videoData, photoData);
//        }
//    };

    private RemoteFileListFragment.OnClickBackListener mOnClickBackListener = new RemoteFileListFragment.OnClickBackListener() {
        @Override
        public void onClickBack() {
            onBackPressed();
        }
    };

    @Override
    public void onCheckedNumChanged(List<RemoteFileBean> checkedList, DownloadCore.OnDownloadProgressChangedListener onDownloadProgressChangedListener) {
        mCheckedList = checkedList;
    }

    private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            switch (position) {
                case 0:
                    mLLVideo.setVisibility(View.VISIBLE);
                    mLLPhoto.setVisibility(View.GONE);
                    break;
                case 1:
                    mLLVideo.setVisibility(View.GONE);
                    mLLPhoto.setVisibility(View.VISIBLE);
                    break;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    @Override
    public void onClick(View v) {
        if (mCheckedList == null || mCheckedList.size() != 1) {
            return;
        }
        switch (v.getId()) {
            case R.id.activity_editfilelist_tv_cut:

                break;
            case R.id.activity_editfilelist_tv_compress:

                break;
            case R.id.activity_editfilelist_ib_shui:
                Intent shuiIntent = new Intent(EditListActivity.this, ImageEditActivity.class);
                shuiIntent.putExtra(ImageEditActivity.KEY_TYPE, ImageEditActivity.SHUI);
                shuiIntent.putExtra(ImageEditActivity.KEY_IMGPATH, FileConstants.LOCAL_PHOTO_PATH + File.separator + mCheckedList.get(0).getName());
                startActivity(shuiIntent);
                break;
            case R.id.activity_editfilelist_tv_text:
                Intent textIntent = new Intent(EditListActivity.this, ImageEditActivity.class);
                textIntent.putExtra(ImageEditActivity.KEY_TYPE, ImageEditActivity.TEXT);
                textIntent.putExtra(ImageEditActivity.KEY_IMGPATH, FileConstants.LOCAL_PHOTO_PATH + File.separator + mCheckedList.get(0).getName());
                startActivity(textIntent);
                break;
            case R.id.activity_editfilelist_tv_filter:
                Intent filterIntent = new Intent(EditListActivity.this, ImageEditActivity.class);
                filterIntent.putExtra(ImageEditActivity.KEY_TYPE, ImageEditActivity.FILTER);
                filterIntent.putExtra(ImageEditActivity.KEY_IMGPATH, FileConstants.LOCAL_PHOTO_PATH + File.separator + mCheckedList.get(0).getName());
                startActivity(filterIntent);
                break;
        }
    }
}
