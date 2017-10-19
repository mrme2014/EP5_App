package com.smd.remotecamera.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ntk.nvtkit.NVTKitModel;
import com.smd.remotecamera.R;
import com.smd.remotecamera.adapter.FileListAdapter;
import com.smd.remotecamera.bean.RemoteFileBean;
import com.smd.remotecamera.constants.FileConstants;
import com.smd.remotecamera.controller.RemoteFileController;
import com.smd.remotecamera.core.DownloadCore;
import com.smd.remotecamera.fragment.RemoteFileListFragment;
import com.smd.remotecamera.service.DownloadService;
import com.smd.remotecamera.util.CommonUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;


public class DownloadListActivity extends AppCompatActivity implements FileListAdapter.OnCheckedNumChangedListener, ServiceConnection, View.OnClickListener,
        DownloadCore.OnDownloadFinishListener {

    private FrameLayout mFlContainer;
    private ImageButton mIbDelete;
    private TextView mTvDownload;
    private TextView mTvEdit;

    private RemoteFileListFragment mRemoteFileListFragment;

    private RemoteFileController mRemoteFileController;
    private DownloadService.DownloadBinder mDownloadBinder;

    private List<RemoteFileBean> mVideoList;
    private List<RemoteFileBean> mPhotoList;
    private List<RemoteFileBean> mCheckedList;

    private DownloadCore.OnDownloadProgressChangedListener mOnDownloadProgressChangedListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloadlist);

        initView();
        init();

        new Thread(new Runnable() {
            @Override
            public void run() {
                final String ack = NVTKitModel.changeMode(NVTKitModel.MODE_PLAYBACK);
                mRemoteFileController.qeryRemoteFileList();
            }
        }).start();

        bindService(new Intent(this, DownloadService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        mDownloadBinder.disableListener();
        unbindService(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mDownloadBinder.checkHasDownloadFile()) {
            showBackDialog();
            return;
        }
        finish();
    }

    private void showBackDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("有文件正在下载，是否停止？");
        builder.setPositiveButton("停止并返回", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                stopService(new Intent(DownloadListActivity.this, DownloadService.class));
                dialog.dismiss();
                finish();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void initView() {
        mFlContainer = (FrameLayout) findViewById(R.id.activity_downloadfilelist_container);
        mIbDelete = (ImageButton) findViewById(R.id.activity_downloadfilelist_ib_delete);
        mTvDownload = (TextView) findViewById(R.id.activity_downloadfilelist_tv_download);
        mTvEdit = (TextView) findViewById(R.id.activity_downloadfilelist_tv_edit);
        mTvDownload.setOnClickListener(this);
        mTvEdit.setOnClickListener(this);
    }

    private void init() {
        mRemoteFileController = new RemoteFileController();
        mRemoteFileController.setOnRemoteFileQueryFinishListener(mOnQueryFinishListener);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        mRemoteFileListFragment = new RemoteFileListFragment(false);
        mRemoteFileListFragment.setOnClickBackListener(mOnClickBackListener);
        mRemoteFileListFragment.setOnCheckedNumChangedListener(this);
        transaction.add(R.id.activity_downloadfilelist_container, mRemoteFileListFragment);
        transaction.commit();
    }

    private void checkDownload() {
        List<RemoteFileBean> tmp = new ArrayList<>(mVideoList);
        tmp.addAll(mPhotoList);
        if (mCheckedList.size() == 0) {
            CommonUtil.showToast(getApplicationContext(), "请选择要下载的文件");
        }
        for (RemoteFileBean remoteFileBean : mCheckedList) {
            for (RemoteFileBean remoteFileBean2 : tmp) {
                if (remoteFileBean.getName().equals(remoteFileBean2.getName())) {
                    if (!remoteFileBean2.isDownloaded()) {
                        if (!mDownloadBinder.checkIsDownloading(remoteFileBean)) {
//                        new DownloadCore(remoteFileBean, mOnDownloadProgressChangedListener).download(this);
                            mDownloadBinder.download(remoteFileBean, this, mOnDownloadProgressChangedListener);
                        }
                    } else {
                        CommonUtil.showLogToast(this, "已经下载了");
                    }
                    continue;
                }
            }
        }
    }

    private RemoteFileController.OnRemoteFileQueryFinishListener mOnQueryFinishListener = new RemoteFileController.OnRemoteFileQueryFinishListener() {
        @Override
        public void onQueryFinished(List<RemoteFileBean> videoData, List<RemoteFileBean> photoData) {
            mVideoList = RemoteFileBean.getList();
            mPhotoList = photoData;
            mRemoteFileListFragment.setData(mVideoList, mPhotoList);
        }
    };

    private RemoteFileListFragment.OnClickBackListener mOnClickBackListener = new RemoteFileListFragment.OnClickBackListener() {
        @Override
        public void onClickBack() {
            onBackPressed();
        }
    };

    @Override
    public void onCheckedNumChanged(List<RemoteFileBean> checkedList, DownloadCore.OnDownloadProgressChangedListener onDownloadProgressChangedListener) {
        mCheckedList = checkedList;
        mOnDownloadProgressChangedListener = onDownloadProgressChangedListener;
        if (mCheckedList.size() == 1 && mCheckedList.get(0).isDownloaded()) {
            mTvEdit.setBackgroundColor(getResources().getColor(R.color.activity_download_btn_enable));
        } else {
            mTvEdit.setBackgroundColor(getResources().getColor(R.color.activity_download_btn_disable));
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mDownloadBinder = (DownloadService.DownloadBinder) service;
        mDownloadBinder.setOnDownloadFinishListener(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                int time = 5000;
                while (time > 0) {
                    if (mOnDownloadProgressChangedListener != null) {
                        mDownloadBinder.setOnDownloadProgressChangedListener(mOnDownloadProgressChangedListener);
                        break;
                    } else {
                        SystemClock.sleep(100);
                        time -= 100;
                    }
                }
            }
        }).start();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_downloadfilelist_tv_download:
                checkDownload();
                break;
            case R.id.activity_downloadfilelist_tv_edit:
                if (mCheckedList.size() != 1) {
                    break;
                }
                if (mCheckedList.get(0).getName().endsWith(FileConstants.POSTFIX_PHOTO)) {
                    Intent shuiIntent = new Intent(DownloadListActivity.this, ImageEditActivity.class);
                    shuiIntent.putExtra(ImageEditActivity.KEY_TYPE, ImageEditActivity.SHUI);
                    shuiIntent.putExtra(ImageEditActivity.KEY_IMGPATH, FileConstants.LOCAL_PHOTO_PATH + File.separator + mCheckedList.get(0).getName());
                    startActivity(shuiIntent);
                }

                break;
        }
    }

    @Override
    public void onDownloadFinish(boolean success, RemoteFileBean remoteFileBean) {
        RemoteFileBean checkedBean = null;
        if (mCheckedList.size() == 1) {
            checkedBean = mCheckedList.get(0);
        }
        RemoteFileBean next;
        ListIterator<RemoteFileBean> videoIterator = mVideoList.listIterator();
        while (videoIterator.hasNext()) {
            next = videoIterator.next();
            if (next.getName().equals(remoteFileBean.getName())) {
                next.setDownloaded(success);
                CommonUtil.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        mTvEdit.setBackgroundColor(getResources().getColor(R.color.activity_download_btn_enable));
                    }
                });
                return;
            }
        }
        ListIterator<RemoteFileBean> photoIterator = mPhotoList.listIterator();
        while (photoIterator.hasNext()) {
            next = photoIterator.next();
            if (next.getName().equals(remoteFileBean.getName())) {
                next.setDownloaded(success);
                if ((checkedBean != null) && checkedBean.getName().equals(remoteFileBean.getName())) {
                    CommonUtil.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            mTvEdit.setBackgroundColor(getResources().getColor(R.color.activity_download_btn_enable));
                        }
                    });
                }
                return;
            }
        }
    }

}
