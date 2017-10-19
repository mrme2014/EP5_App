package com.smd.remotecamera.util;


import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.WindowManager;

import com.smd.remotecamera.constants.FileConstants;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import static com.smd.remotecamera.constants.FileConstants.LOCAL_PHOTO_PATH;
import static com.smd.remotecamera.constants.FileConstants.LOCAL_THUMB_PATH;
import static com.smd.remotecamera.constants.FileConstants.LOCAL_VIDEO_PATH;
import static com.smd.remotecamera.constants.FileConstants.POSTFIX_PHOTO;
import static com.smd.remotecamera.constants.FileConstants.POSTFIX_VIDEO_CUSTOM_THUMB;

public class Util {
    public static Pair<Integer, Integer> getScreenSize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        Pair<Integer, Integer> screenSize = new Pair<>(displayMetrics.widthPixels, displayMetrics.heightPixels);
        return screenSize;
    }

    public static String getDate(long timeMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        return "" + calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.MONTH) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
    }

    public static String getTime(long timeMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        return "" + calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND);
    }

    public static String getSizeOfM(long size) {
        float sizeofM = 1.0F * size / 1024 / 1024;
        return String.format("%.2fM", sizeofM);
    }

    public static void checkLocalFolder() {
        File photo = new File(FileConstants.LOCAL_PHOTO_PATH);
        if (!photo.exists()) {
            photo.mkdirs();
        }
        File video = new File(FileConstants.LOCAL_VIDEO_PATH);
        if (!video.exists()) {
            video.mkdirs();
        }
        File thumb = new File(LOCAL_THUMB_PATH);
        if (!thumb.exists()) {
            thumb.mkdirs();
        }
    }

    public static String getTimeStr(String fileName) {
        return fileName.substring(0, 4) + "/" + fileName.substring(5, 7) + "/" + fileName.substring(7, 9) + " " + fileName.substring(10, 12) + ":" + fileName.substring(12, 14) + ":" + fileName.substring(14, 16);
    }

    public static String getLocalThumb(String fileName) {
        if (fileName.endsWith(POSTFIX_PHOTO)) {
            return getLocalPhotoThumb(fileName);
        } else {
            return getLocalVideoThumb(fileName);
        }
    }

    public static String getLocalPhotoThumb(String fileName) {
        return LOCAL_PHOTO_PATH + File.separator + fileName;
    }

    public static String getLocalVideoThumb(String fileName) {
        String thumbFileName = fileName.substring(0, fileName.lastIndexOf('.'))
                + POSTFIX_VIDEO_CUSTOM_THUMB;
        File thumbFile = new File(LOCAL_THUMB_PATH, thumbFileName);
        if (thumbFile.exists()) {
            return thumbFile.getAbsolutePath();
        } else {
            return createThumbnailSaveDefault(LOCAL_VIDEO_PATH + File.separator + fileName);
        }
    }

    private static String createThumbnailSaveDefault(String videoPath) {
        Bitmap thumbnailBitmap = createVideoThumbnail(videoPath,
                320, 240,
                MediaStore.Images.Thumbnails.MINI_KIND);
        String folderPath = LOCAL_THUMB_PATH;
        String fileName = videoPath.substring(videoPath
                .lastIndexOf(File.separator) + 1);
        fileName = fileName.substring(0, fileName.lastIndexOf('.'))
                + POSTFIX_VIDEO_CUSTOM_THUMB;
        String filePath = null;
        try {
            filePath = Util.saveToFile(folderPath, fileName,
                    thumbnailBitmap, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (thumbnailBitmap != null) {
            thumbnailBitmap.recycle();
        }
        return filePath;
    }

    private static Bitmap createVideoThumbnail(String videoPath, int width,
                                               int height, int kind) {
        Bitmap bitmap = null;
        // 创建一个缩略图
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
        // 按指定大小创建一个缩略图
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    public static String saveToFile(String folderPath, String fileName,
                                     Bitmap bitmap, boolean createNewFile) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            return saveToFile(folderPath, fileName, data, createNewFile);
        }
        return null;
    }

    private static String saveToFile(String folderPath, String fileName,
                                     byte[] data, boolean createNewFile) throws IOException {
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File file = new File(folder, fileName);
        if (file.exists()) {
            if (createNewFile) {
                file.delete();
                file.createNewFile();
            } else {
                return null;
            }
        } else {
            file.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(data);
        if (fos != null) {
            fos.close();
        }
        return file.getAbsolutePath();
    }
}
