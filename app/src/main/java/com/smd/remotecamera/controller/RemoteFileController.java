package com.smd.remotecamera.controller;


import android.util.Log;

import com.ntk.nvtkit.NVTKitModel;
import com.ntk.util.ParseResult;
import com.ntk.util.Util;
import com.smd.remotecamera.bean.RemoteFileBean;
import com.smd.remotecamera.constants.FileConstants;
import com.smd.remotecamera.core.ImageDownloaderTask;
import com.smd.remotecamera.util.CommonUtil;
import com.smd.remotecamera.view.SquareImageView;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RemoteFileController {

    private OnRemoteFileQueryFinishListener mOnQueryFinishListener;

    public RemoteFileController() {

    }

    public void qeryRemoteFileList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<RemoteFileBean> videoData = new ArrayList<>();
                final List<RemoteFileBean> photoData = new ArrayList<>();
                ParseResult result = NVTKitModel.getFileList();
                File tmpFile = null;
                if (result != null && result.getFileItemList() != null) {
                    for (int i = 0; i < result.getFileItemList().size(); i++) {
                        final String name = result.getFileItemList().get(i).NAME;
                        String urlStr = result.getFileItemList().get(i).FPATH;
                        URL url = getURLFromPath(urlStr);
                        long size = Long.valueOf(result.getFileItemList().get(i).SIZE);
                        String timeStr = result.getFileItemList().get(i).TIME;
                        if (name.endsWith(FileConstants.POSTFIX_VIDEO)) {
                            tmpFile = new File(FileConstants.LOCAL_VIDEO_PATH + File.separator + name);
                            RemoteFileBean remoteFileBean = new RemoteFileBean(name, url, size, timeStr, (tmpFile.exists() && tmpFile.length() == size));
                            videoData.add(remoteFileBean);
                        } else if (name.endsWith(FileConstants.POSTFIX_PHOTO)) {
                            tmpFile = new File(FileConstants.LOCAL_PHOTO_PATH + File.separator + name);
                            RemoteFileBean remoteFileBean = new RemoteFileBean(name, url, size, timeStr, (tmpFile.exists() && tmpFile.length() == size));
                            photoData.add(remoteFileBean);
                        }
                    }
                }
                CommonUtil.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mOnQueryFinishListener != null) {
                            mOnQueryFinishListener.onQueryFinished(videoData, photoData);
                        }
                    }
                });
            }
        }).start();
    }

    public List<String> queryLocalFileList(String path) {
        File filePath = new File(path);
        File[] files = filePath.listFiles();
        List<String> result = new ArrayList<>();
        for (File file : files) {
            result.add(file.getName());
        }
        return result;
    }

    private URL getURLFromPath(String filePath) {
        String urlStr = filePath.replace("A:", "http://" + Util.getDeciceIP() + "");
        urlStr = urlStr.replace("\\", "/");
        URL url = null;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public void loadImage(SquareImageView iv, String url, String name) {
        new ImageDownloaderTask(iv).execute(url, name);
    }

    public void setOnRemoteFileQueryFinishListener(OnRemoteFileQueryFinishListener onRemoteFileQueryFinishListener) {
        mOnQueryFinishListener = onRemoteFileQueryFinishListener;
    }

    public interface OnRemoteFileQueryFinishListener {
        void onQueryFinished(List<RemoteFileBean> videoData, List<RemoteFileBean> photoData);
    }
}
