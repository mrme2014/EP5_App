package com.smd.remotecamera.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qiaomu.libvideo.VideoTranscodeActivity;
import com.qiaomu.libvideo.VideoTrimActivity;
import com.qiaomu.libvideo.utils.AppUtils;
import com.qiaomu.libvideo.utils.CompressUtils;
import com.qiaomu.libvideo.utils.ToastUtils;
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
    private boolean isVideoEmpty, isPhotoEmpty;
    private boolean isTranscodeOk;
    private int mCurPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
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
        findViewById(R.id.activity_downloadfilelist_ib_delete1).setOnClickListener(makeListener());
        findViewById(R.id.activity_downloadfilelist_ib_delete).setOnClickListener(makeListener());
    }

    private View.OnClickListener makeListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCheckedList == null || mCheckedList.size() == 0)
                    return;
                AppUtils.showAlertDialog(EditListActivity.this, "确定要删除该文件吗?", R.string.ok, R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == -1) {
                            RemoteFileBean remoteFileBean = mCheckedList.remove(0);
                            File file = new File(mCurPager == 0 ? FileConstants.LOCAL_VIDEO_PATH : FileConstants.LOCAL_PHOTO_PATH, remoteFileBean.getName());
                            file.delete();
                            Toast.makeText(getApplicationContext(), "删除成功", Toast.LENGTH_SHORT).show();
                            boolean isEmpty = mRemoteFileListFragment.ondeleted(remoteFileBean);
                            if (isEmpty && mCurPager == 0) {
                                mLLVideo.setVisibility(View.GONE);
                                isVideoEmpty = true;
                            }
                            if (isEmpty && mCurPager == 1) {
                                isPhotoEmpty = true;
                                mLLPhoto.setVisibility(View.GONE);
                            }


                        }
                    }
                });
            }
        };
    }

    private void init() {
//        mRemoteFileController = new RemoteFileController();
//        mRemoteFileController.setOnRemoteFileQueryFinishListener(mOnQueryFinishListener);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        mRemoteFileListFragment = new RemoteFileListFragment(true, FileListAdapter.FileNameType.DOWNLOAD);
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
                    if (videoFiles != null) {
                        for (File file : videoFiles) {
                            tmpBean = new RemoteFileBean(file.getName(), null, file.length(), Util.getTimeStr(file.getName()), true);
                            mVideoList.add(tmpBean);
                        }
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
                    if (photoFiles != null) {
                        for (File file : photoFiles) {
                            tmpBean = new RemoteFileBean(file.getName(), null, file.length(), Util.getTimeStr(file.getName()), true);
                            String checkFileName = "0" + file.getName().substring(0, file.getName().lastIndexOf(".")) + FileConstants.POSTFIX_PHOTO_EDIT;
                            File checkFile = new File(FileConstants.LOCAL_EDIT_PATH, checkFileName);
                            tmpBean.setHasEdit(checkFile.exists());
                            mPhotoList.add(tmpBean);

                        }
                    }
                }
                mRemoteFileListFragment.setData(mVideoList, mPhotoList);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isVideoEmpty = mVideoList == null || mVideoList.size() == 0;
                        isPhotoEmpty = mPhotoList == null || mPhotoList.size() == 0;
                        if (isVideoEmpty)
                            mLLVideo.setVisibility(View.GONE);
                    }
                });

            }
        }).start();
    }


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
            mCurPager = position;
            switch (position) {
                case 0:
                    if (!isVideoEmpty) {
                        mLLVideo.setVisibility(View.VISIBLE);
                    }
                    mLLPhoto.setVisibility(View.GONE);
                    break;
                case 1:
                    if (!isPhotoEmpty) {
                        mLLPhoto.setVisibility(View.VISIBLE);
                    }
                    mLLVideo.setVisibility(View.GONE);
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
            ToastUtils.s(this, "请至多选择一个视频或图片文件!");
            return;
        }

        switch (v.getId()) {
            case R.id.activity_editfilelist_tv_cut: {
                String file_path = FileConstants.LOCAL_VIDEO_PATH + File.separator + mCheckedList.get(0).getName();
                File file = new File(file_path);
                if (!file.exists()) {
                    ToastUtils.s(this, "文件不存在");
                    return;
                }
                VideoTrimActivity.startTrimActivity(this, file_path);
            }
            break;
            case R.id.activity_editfilelist_tv_mp4:
            case R.id.activity_editfilelist_tv_compress:
                String file_path = FileConstants.LOCAL_VIDEO_PATH + File.separator + mCheckedList.get(0).getName();
                CompressUtils.compress(this, file_path, new CompressUtils.CompressListener() {
                    @Override
                    public void onResult(boolean success) {
                        isTranscodeOk = true;
                    }
                });

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
