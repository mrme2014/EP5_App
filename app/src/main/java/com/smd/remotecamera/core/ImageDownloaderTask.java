package com.smd.remotecamera.core;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;

import com.ntk.nvtkit.NVTKitModel;
import com.smd.remotecamera.R;
import com.smd.remotecamera.constants.FileConstants;
import com.smd.remotecamera.util.Util;
import com.smd.remotecamera.view.SquareImageView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class ImageDownloaderTask extends AsyncTask<String, Void, Uri> {

    private final WeakReference<SquareImageView> imageViewReference;

    public ImageDownloaderTask(SquareImageView imageView) {
        imageViewReference = new WeakReference<>(imageView);
    }

    @Override
    protected Uri doInBackground(String... params) {
        Util.checkLocalFolder();
        return downloadBitmap(params[0], params[1]);
    }

    @Override
    protected void onPostExecute(Uri uri) {
        if (isCancelled()) {
            uri = null;
        }

        if (imageViewReference != null) {
            SquareImageView imageView = imageViewReference.get();
            if (imageView != null) {
                Picasso.with(imageView.getContext()).load(uri)
                        .placeholder(R.drawable.timg)
                        .error(R.drawable.timg)
                        .noFade()
                        .fit().into(imageView);
            }

        }
    }

    private Uri downloadBitmap(String url, String name) {
        File file = new File(FileConstants.LOCAL_THUMB_PATH, name);
        FileOutputStream fos = null;

        Bitmap bitmap = NVTKitModel.getThumbnailImageFromURL(url);
        if (bitmap == null) {
            return null;
        }
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        bitmap.recycle();
        bitmap = null;
        return Uri.fromFile(file);
    }

}