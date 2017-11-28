package com.smd.remotecamera.constants;


import android.os.Environment;

import com.smd.remotecamera.bean.RemoteFileBean;

public class FileConstants {

//    public static final String LOCAL_PHOTO_PATH = Environment.getExternalStorageDirectory() + "/" + "remoteCam/Image";
//    public static final String LOCAL_VIDEO_PATH = Environment.getExternalStorageDirectory() + "/" + "remoteCam/Video";
//    public static final String LOCAL_THUMB_PATH = Environment.getExternalStorageDirectory() + "/" + "remoteCam/thumb";

    public static final String LOCAL_PHOTO_PATH = Environment.getExternalStorageDirectory() + "/" + "C-Video/Image";
    public static final String LOCAL_VIDEO_PATH = Environment.getExternalStorageDirectory() + "/" + "C-Video/Video";
    public static final String LOCAL_THUMB_PATH = Environment.getExternalStorageDirectory() + "/" + "C-Video/thumb";
    public static final String LOCAL_EDIT_PATH = Environment.getExternalStorageDirectory() + "/" + "C-Video/Edit";
    public static final String LOCAL_TRANSCODE_PATH = Environment.getExternalStorageDirectory() + "/" + "C-Video/Transcode";
    public static final String LOCAL_TRIMMED_PATH = Environment.getExternalStorageDirectory() + "/" + "C-Video/Trim";

    public static final String POSTFIX_PHOTO = "JPG";
    public static final String POSTFIX_PHOTO_EDIT = "_et" + "." + POSTFIX_PHOTO;
    public static final String POSTFIX_VIDEO = "MOV";
    public static final String POSTFIX_VIDEO_CUSTOM_THUMB = "_cst" + "." + POSTFIX_PHOTO;

    public static final String FONT_HEITI = "font_heiti.ttf";
    public static final String FONT_HUAWENLLISHU = "font_huawenlishu.TTF";
    public static final String FONT_HUAWENSONGTI = "font_huawensongti.TTF";
    public static final String FONT_HUAWENXINGKAI = "font_huawenxingkai.TTF";

    public static String getLocalPath(RemoteFileBean remoteFileBean) {
        if (remoteFileBean.getName().endsWith(POSTFIX_PHOTO)) {
            return LOCAL_PHOTO_PATH;
        } else if (remoteFileBean.getName().endsWith(POSTFIX_VIDEO)) {
            return LOCAL_VIDEO_PATH;
        } else {
            return LOCAL_PHOTO_PATH;
        }
    }
}
