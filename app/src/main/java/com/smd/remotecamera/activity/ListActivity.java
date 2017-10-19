//package com.smd.remotecamera.activity;
//
//import android.app.Activity;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.os.Environment;
//import android.support.annotation.Nullable;
//import android.util.Log;
//import android.util.Pair;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.AdapterView;
//import android.widget.BaseAdapter;
//import android.widget.ListView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.ntk.nvtkit.NVTKitModel;
//import com.ntk.util.ParseResult;
//import com.ntk.util.Util;
//import com.smd.remotecamera.R;
//
//import java.io.BufferedInputStream;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.net.URL;
//import java.net.URLConnection;
//import java.util.ArrayList;
//import java.util.List;
//
//
//public class ListActivity extends Activity {
//
//        private ListView mLv;
//
//        private List<Pair<String, String>> mData;
//
//        @Override
//        protected void onCreate(@Nullable Bundle savedInstanceState) {
//                super.onCreate(savedInstanceState);
//                setContentView(R.layout.activity_list);
//                mLv = (ListView) findViewById(R.id.lv);
//
//                init();
//
//                if (mToast == null) {
//                        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
//                }
//
//                File file = new File(Environment.getExternalStorageDirectory() + "/" + "remoteCam/Image");
//                File file1 = new File(Environment.getExternalStorageDirectory() + "/" + "remoteCam/Video");
//                if (!file.exists()) {
//                        file.mkdirs();
//                }
//                if (!file1.exists()) {
//                        file1.mkdirs();
//                }
//        }
//
//        private void init() {
//                mData = new ArrayList<>();
//                new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                                ParseResult result = NVTKitModel.getFileList();
//                                if (result.getFileItemList() != null) {
//                                        for (int i = 0; i < result.getFileItemList().size(); i++) {
//                                                final String a = result.getFileItemList().get(i).NAME;
//                                                String url = result.getFileItemList().get(i).FPATH;
//                                                String url1 = url.replace("A:", "http://" + Util.getDeciceIP() + "");
//                                                String url2 = url1.replace("\\", "/");
//                                                mData.add(new Pair<String, String>(url2, a));
//                                                runOnUiThread(new Runnable() {
//                                                        @Override
//                                                        public void run() {
////                                Toast.makeText(ListActivity.this, b, Toast.LENGTH_SHORT).show();
//                                                        }
//                                                });
//                                        }
//                                }
//                                runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                                initListView();
//                                        }
//                                });
//                        }
//                }).start();
//        }
//
//        private void initListView() {
//                MyAdapter myAdapter = new MyAdapter();
//                mLv.setAdapter(myAdapter);
//                mLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                        @Override
//                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                                new DownloadFileFromURL().execute(mData.get(position).first, mData.get(position).second);
//                        }
//                });
//        }
//
//        private Toast mToast = null;
//
//        class DownloadFileFromURL extends AsyncTask<String, String, String> {
//
//                /**
//                 * Before starting background thread
//                 * Show Progress Bar Dialog
//                 */
//                @Override
//                protected void onPreExecute() {
//                        super.onPreExecute();
//                }
//
//                /**
//                 * Downloading file in background thread
//                 */
//                @Override
//                protected String doInBackground(String... params) {
//                        runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
////                    Toast.makeText(ListActivity.this, "begin", Toast.LENGTH_SHORT).show();
//                                }
//                        });
//                        String file_name;
//                        String path = "null";
//
//                        int count;
//                        try {
//                                file_name = params[1];
//                                URL url = new URL(params[0]);
//                                URLConnection conection = url.openConnection();
//                                conection.connect();
//                                // this will be useful so that you can show a tipical 0-100% progress bar
//                                int lenghtOfFile = conection.getContentLength();
//
//                                // download the file
//                                InputStream input = new BufferedInputStream(url.openStream(), 8192);
//
//                                // Output stream
//                                OutputStream output;
//                                if (Util.isContainExactWord(file_name, "JPG")) {
//                                        path = Environment.getExternalStorageDirectory() + "/" + "remoteCam/Image" + "/" + file_name;
//                                } else {
//                                        path = Environment.getExternalStorageDirectory() + "/" + "remoteCam/Video" + "/" + file_name;
//                                }
//                                output = new FileOutputStream(path);
//
//
//                                byte data[] = new byte[1024];
//
//                                long total = 0;
//
//                                while ((count = input.read(data)) != -1) {
//                                        total += count;
//                                        // publishing the progress....
//                                        // After this onProgressUpdate will be called
//                                        publishProgress("" + (int) ((total * 100) / lenghtOfFile));
//
//                                        // writing data to file
//                                        output.write(data, 0, count);
//                                }
//
//                                // flushing output
//                                output.flush();
//
//                                // closing streams
//                                output.close();
//                                input.close();
//
//                        } catch (Exception e) {
//                                Log.e("Error: ", e.getMessage());
//                        }
//
//                        return path;
//                }
//
//                /**
//                 * Updating progress bar
//                 */
//                protected void onProgressUpdate(String... progress) {
//                        // setting progress percentage
//                        mToast.setText("已下载：" + Integer.parseInt(progress[0]));
//                        mToast.show();
//                }
//
//                /**
//                 * After completing background task
//                 * Dismiss the progress dialog
//                 **/
//                @Override
//                protected void onPostExecute(String file_url) {
//                        // dismiss the dialog after the file was downloaded
//                        mToast.setText("下载完成：" + file_url);
//                        mToast.show();
//                }
//
//        }
//
//        private class MyAdapter extends BaseAdapter {
//
//                @Override
//                public int getCount() {
//                        return mData.size();
//                }
//
//                @Override
//                public Object getItem(int position) {
//                        return mData.get(position);
//                }
//
//                @Override
//                public long getItemId(int position) {
//                        return position;
//                }
//
//                @Override
//                public View getView(int position, View convertView, ViewGroup parent) {
//                        convertView = LayoutInflater.from(ListActivity.this).inflate(R.layout.item, parent, false);
//                        TextView textView = (TextView) convertView.findViewById(R.id.item);
//                        textView.setText(mData.get(position).second);
//                        return convertView;
//                }
//        }
//}
