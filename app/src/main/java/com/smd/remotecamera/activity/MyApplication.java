package com.smd.remotecamera.activity;

import android.app.Application;

import com.smd.remotecamera.constants.FileConstants;

import java.io.File;


public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        File photoFolder = new File(FileConstants.LOCAL_PHOTO_PATH);
        File videoFolder = new File(FileConstants.LOCAL_VIDEO_PATH);
        File thumbFolder = new File(FileConstants.LOCAL_THUMB_PATH);
        File editFolder = new File(FileConstants.LOCAL_EDIT_PATH);
        if (!photoFolder.exists()) {
            photoFolder.mkdirs();
        }
        if (!videoFolder.exists()) {
            videoFolder.mkdirs();
        }
        if (!thumbFolder.exists()) {
            thumbFolder.mkdirs();
        }
        if (!editFolder.exists()) {
            editFolder.mkdirs();
        }
    }
}
