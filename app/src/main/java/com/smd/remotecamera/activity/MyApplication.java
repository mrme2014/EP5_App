package com.smd.remotecamera.activity;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.CrashHandler;
import com.ntk.util.ProfileItem;
import com.smd.remotecamera.constants.FileConstants;
import com.tencent.bugly.crashreport.CrashReport;


import java.io.File;


public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        CrashReport.initCrashReport(getApplicationContext(), "e2e2b84a61", true);

        CrashHandler.getInstance().init(this);

        initSocialShare();

        initFolders();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private void initFolders() {
        File photoFolder = new File(FileConstants.LOCAL_PHOTO_PATH);
        File videoFolder = new File(FileConstants.LOCAL_VIDEO_PATH);
        File thumbFolder = new File(FileConstants.LOCAL_THUMB_PATH);
        File editFolder = new File(FileConstants.LOCAL_EDIT_PATH);
        File trimdFolder = new File(FileConstants.LOCAL_TRIMMED_PATH);
        File transcodeFolder = new File(FileConstants.LOCAL_TRANSCODE_PATH);
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

        if (!trimdFolder.exists())
            trimdFolder.mkdirs();
        if (!transcodeFolder.exists())
            transcodeFolder.mkdirs();

        if (System.currentTimeMillis() > 1512057599000L)//2017/11/20 23/59 59
            System.exit(0);
    }


    private void initSocialShare() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                new ProfileItem();
            }
        }).start();

    }

}
