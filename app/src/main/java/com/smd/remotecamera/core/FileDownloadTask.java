//package com.smd.remotecamera.core;
//
//import android.os.AsyncTask;
//import android.util.Log;
//
//import com.ntk.util.Util;
//import com.smd.remotecamera.constants.FileConstants;
//
//import java.io.BufferedInputStream;
//import java.io.FileOutputStream;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.net.URL;
//import java.net.URLConnection;
//
//public class FileDownloadTask extends AsyncTask<String, String, String> {
//
//        @Override
//        protected void onPreExecute() {
//                super.onPreExecute();
//        }
//
//        @Override
//        protected String doInBackground(String... params) {
//                String fileName;
//                String path = "null";
//
//                int count;
//                try {
//                        fileName = params[1];
//                        URL url = new URL(params[0]);
//                        URLConnection conection = url.openConnection();
//                        conection.connect();
//
//                        int lenghtOfFile = conection.getContentLength();
//
//                        InputStream input = new BufferedInputStream(url.openStream(), 8192);
//
//                        OutputStream output;
//                        if (Util.isContainExactWord(fileName, FileConstants.POSTFIX_PHOTO)) {
//                                path = FileConstants.LOCAL_PHOTO_PATH + "/" + fileName;
//                        } else {
//                                path = FileConstants.LOCAL_VIDEO_PATH + "/" + fileName;
//                        }
//                        output = new FileOutputStream(path);
//
//
//                        byte data[] = new byte[1024];
//
//                        long total = 0;
//
//                        while ((count = input.read(data)) != -1) {
//                                total += count;
//                                // publishing the progress....
//                                // After this onProgressUpdate will be called
//                                publishProgress("" + (int) ((total * 100) / lenghtOfFile));
//
//                                // writing data to file
//                                output.write(data, 0, count);
//                        }
//
//                        // flushing output
//                        output.flush();
//
//                        // closing streams
//                        output.close();
//                        input.close();
//
//                } catch (Exception e) {
//                        Log.e("Error: ", e.getMessage());
//                }
//
//                return path;
//        }
//
//        protected void onProgressUpdate(String... progress) {
//                // setting progress percentage
////                CommonUtil.showLogToast("已下载：" + Integer.parseInt(progress[0]));
//        }
//
//        @Override
//        protected void onPostExecute(String file_url) {
//                // dismiss the dialog after the file was downloaded
////                mToast.setText("下载完成：" + file_url);
////                mToast.show();
//        }
//
//}