package com.smd.remotecamera.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.smd.remotecamera.bean.RemoteFileBean;
import com.smd.remotecamera.core.DownloadCore;

import java.util.ArrayList;
import java.util.List;


public class DownloadService extends Service {
    private static final String TAG = "DownloadService";

    private List<DownloadCore.OnDownloadFinishListener> mExtOnDownloadFinishListenerList = new ArrayList<>();
    private List<DownloadCore.OnDownloadProgressChangedListener> mExtOnDownloadProgressChangedListenerList = new ArrayList<>();

    private List<RemoteFileBean> mDownloadingData = new ArrayList<>();
    private List<DownloadCore> mDownloadCoreList = new ArrayList<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new DownloadBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        for (DownloadCore downloadCore : mDownloadCoreList) {
            downloadCore.stopDownload();
        }
        super.onDestroy();
    }

    private void startDownload(RemoteFileBean remoteFileBean, DownloadCore.OnDownloadFinishListener onDownloadFinishListener
            , DownloadCore.OnDownloadProgressChangedListener onDownloadProgressChangedListener) {
//        Download d = new Download(remoteFileBean, remoteFileBean.getUrl().toString(), FileConstants.getLocalPath(remoteFileBean));
//        d.setOnDownloadListener(mOnDownloadListener);
//        d.start(false);
        mExtOnDownloadFinishListenerList.add(onDownloadFinishListener);
        mExtOnDownloadProgressChangedListenerList.add(onDownloadProgressChangedListener);
        DownloadCore downloadCore = new DownloadCore(remoteFileBean, mOnDownloadProgressChangedListener);
        mDownloadCoreList.add(downloadCore);
        mDownloadingData.add(remoteFileBean);
        downloadCore.download(mOnDownloadFinishListener);
    }

//    private Download.OnDownloadListener mOnDownloadListener = new Download.OnDownloadListener() {
//        @Override
//        public void onSuccess(RemoteFileBean downloadFile) {
//            CommonUtil.SYSO(TAG, downloadFile.getName() + "下载成功");
//        }
//
//        @Override
//        public void onStart(RemoteFileBean downloadFile, long fileSize) {
//            CommonUtil.SYSO(TAG, downloadFile.getName() + "开始下载，文件大小：" + fileSize);
//        }
//
//        @Override
//        public void onPublish(RemoteFileBean downloadFile, long size) {
//            CommonUtil.SYSO(TAG, "更新文件" + downloadFile.getName() + "大小：" + size);
//        }
//
//        @Override
//        public void onPause(RemoteFileBean downloadFile) {
//            CommonUtil.SYSO(TAG, "暂停下载" + downloadFile.getName());
//        }
//
//        @Override
//        public void onGoon(RemoteFileBean downloadFile, long localSize) {
//            CommonUtil.SYSO(TAG, "继续下载" + downloadFile.getName());
//        }
//
//        @Override
//        public void onError(RemoteFileBean downloadFile) {
//            CommonUtil.SYSO(TAG, "下载出错" + downloadFile.getName());
//        }
//
//        @Override
//        public void onCancel(RemoteFileBean downloadFile) {
//            CommonUtil.SYSO(TAG, "取消下载" + downloadFile.getName());
//        }
//    };

    public class DownloadBinder extends Binder {
        public void download(RemoteFileBean remoteFileBean, DownloadCore.OnDownloadFinishListener onDownloadFinishListener, DownloadCore.OnDownloadProgressChangedListener onDownloadProgressChangedListener) {
            startDownload(remoteFileBean, onDownloadFinishListener, onDownloadProgressChangedListener);
        }

        public boolean checkIsDownloading(RemoteFileBean remoteFileBean) {
            return mDownloadingData.contains(remoteFileBean);
        }

        public boolean checkHasDownloadFile() {
            return !mDownloadingData.isEmpty();
        }

        public void setOnDownloadFinishListener(DownloadCore.OnDownloadFinishListener onDownloadFinishListener) {
            mExtOnDownloadFinishListenerList.add(onDownloadFinishListener);
        }

        public void setOnDownloadProgressChangedListener(DownloadCore.OnDownloadProgressChangedListener onDownloadProgressChangedListener) {
            mExtOnDownloadProgressChangedListenerList.add(onDownloadProgressChangedListener);
        }

        public void disableListener() {
            mExtOnDownloadFinishListenerList.clear();
            mExtOnDownloadProgressChangedListenerList.clear();
        }
    }

    private DownloadCore.OnDownloadFinishListener mOnDownloadFinishListener = new DownloadCore.OnDownloadFinishListener() {
        @Override
        public void onDownloadFinish(boolean success, RemoteFileBean remoteFileBean) {
            mDownloadingData.remove(remoteFileBean);
            for (DownloadCore.OnDownloadFinishListener onDownloadFinishListener : mExtOnDownloadFinishListenerList) {
                if (onDownloadFinishListener != null) {
                    onDownloadFinishListener.onDownloadFinish(success, remoteFileBean);
                }
            }
        }
    };

    private DownloadCore.OnDownloadProgressChangedListener mOnDownloadProgressChangedListener = new DownloadCore.OnDownloadProgressChangedListener() {
        @Override
        public void onDownloadProgressChanged(RemoteFileBean remoteFileBean, long maxProgress, long progress) {
            for (DownloadCore.OnDownloadProgressChangedListener onDownloadProgressChangedListener : mExtOnDownloadProgressChangedListenerList) {
                if (onDownloadProgressChangedListener != null) {
                    onDownloadProgressChangedListener.onDownloadProgressChanged(remoteFileBean, maxProgress, progress);
                }
            }
        }
    };
}
