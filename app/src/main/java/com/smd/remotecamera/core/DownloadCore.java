package com.smd.remotecamera.core;


import com.smd.remotecamera.bean.RemoteFileBean;
import com.smd.remotecamera.constants.FileConstants;
import com.smd.remotecamera.util.CommonUtil;
import com.smd.remotecamera.util.OkHttpUtil;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class DownloadCore {
    private static final String TAG = "DownloadCore";

    private RemoteFileBean mRemoteFileBean;
    private OnDownloadProgressChangedListener mOnDownloadProgressChangedListener;

    private long mTotalSize;
    private FileCore mFileCore;

    public DownloadCore(RemoteFileBean remoteFileBean, OnDownloadProgressChangedListener onDownloadProgressChangedListener) {
        mRemoteFileBean = remoteFileBean;
        mOnDownloadProgressChangedListener = onDownloadProgressChangedListener;
        mFileCore = new FileCore();
    }

    public void download(final OnDownloadFinishListener onDownloadFinishListener) {
        if (mRemoteFileBean == null) {
            return;
        }
        OkHttpUtil.getAsynchronous(mRemoteFileBean.getUrl().toString(), null, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                CommonUtil.SYSO(TAG, "下载请求响应失败");
                if (onDownloadFinishListener != null) {
                    onDownloadFinishListener.onDownloadFinish(false, mRemoteFileBean);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                CommonUtil.SYSO(TAG, "下载请求响应成功");
                mTotalSize = response.body().contentLength();
                boolean result = mFileCore.saveToFile(response.body().byteStream(),
                        FileConstants.getLocalPath(mRemoteFileBean) + File.separator + mRemoteFileBean.getName(),
                        mOnPrgListener);
                if (onDownloadFinishListener != null) {
                    onDownloadFinishListener.onDownloadFinish(result, mRemoteFileBean);
                }
            }
        });
    }

    public void stopDownload() {
        mFileCore.stop();
    }

    private FileCore.OnPrgListener mOnPrgListener = new FileCore.OnPrgListener() {
        @Override
        public void onPrgChanged(int prg) {
            if (mOnDownloadProgressChangedListener != null) {
                mOnDownloadProgressChangedListener.onDownloadProgressChanged(mRemoteFileBean, mTotalSize, prg);
            }
        }
    };

    public interface OnDownloadFinishListener {
        void onDownloadFinish(boolean success, RemoteFileBean remoteFileBean);
    }

    public interface OnDownloadProgressChangedListener {
        void onDownloadProgressChanged(RemoteFileBean remoteFileBean, long maxProgress, long progress);
    }

}
