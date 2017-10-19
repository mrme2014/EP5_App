//package com.smd.remotecamera.core;
//
//import android.os.Handler;
//import android.os.Message;
//
//import com.smd.remotecamera.bean.RemoteFileBean;
//import com.smd.remotecamera.util.CommonUtil;
//
//import org.apache.http.HttpResponse;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.conn.params.ConnManagerParams;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.params.BasicHttpParams;
//import org.apache.http.params.HttpConnectionParams;
//import org.apache.http.params.HttpParams;
//import org.apache.http.params.HttpProtocolParams;
//import org.apache.http.protocol.HTTP;
//
//import java.io.File;
//import java.io.InputStream;
//import java.io.RandomAccessFile;
//import java.io.Serializable;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//public class Download implements Serializable {
//
//    private static final String TAG = "Download";
//
//    private static final int START = 1;                 // 开始下载
//    private static final int PUBLISH = 2;               // 更新进度
//    private static final int PAUSE = 3;                 // 暂停下载
//    private static final int CANCEL = 4;                // 取消下载
//    private static final int ERROR = 5;                 // 下载错误
//    private static final int SUCCESS = 6;               // 下载成功
//    private static final int GOON = 7;                  // 继续下载
//
//    private static ExecutorService mThreadPool;         // 线程池
//
//    static {
//        mThreadPool = Executors.newFixedThreadPool(5);  // 默认5个
//    }
//
//    private RemoteFileBean mDownloadFile;                                // 下载的文件
//    private String mFileName;                               // 本地保存文件名
//    private String mUrl;                                            // 下载地址
//    private String mLocalPath;                          // 本地存放目录
//
//    private boolean isPause = false;                    // 是否暂停
//    private boolean isCanceled = false;                 // 是否手动停止下载
//
//    private OnDownloadListener mListener;               // 监听器
//
//    /**
//     * 配置下载线程池的大小
//     *
//     * @param maxSize 同时下载的最大线程数
//     */
//    public static void configDownloadTheadPool(int maxSize) {
//        mThreadPool = Executors.newFixedThreadPool(maxSize);
//    }
//
//    /**
//     * 添加下载任务
//     *
//     * @param downloadFile 下载任务的文件
//     * @param url        下载地址
//     * @param localPath  本地存放地址
//     */
//    public Download(RemoteFileBean downloadFile, String url, String localPath) {
//        if (!new File(localPath).exists()) {
//            new File(localPath).mkdirs();
//        }
//
//        CommonUtil.LOGD(TAG, "下载地址；" + url);
//
//        mDownloadFile = downloadFile;
//        mUrl = url;
//        String[] tempArray = url.split("/");
//        mFileName = tempArray[tempArray.length - 1];
//        mLocalPath = localPath;
//    }
//
//    /**
//     * 设置监听器
//     *
//     * @param listener 设置下载监听器
//     * @return this
//     */
//    public Download setOnDownloadListener(OnDownloadListener listener) {
//        mListener = listener;
//        return this;
//    }
//
//    /**
//     * 获取文件名
//     *
//     * @return 文件名
//     */
//    public String getFileName() {
//        return mFileName;
//    }
//
//    /**
//     * 开始下载
//     * params isGoon是否为继续下载
//     */
//    public void start(final boolean isGoon) {
//        // 处理消息
//        final Handler handler = new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//                switch (msg.what) {
//                    case ERROR:
//                        mListener.onError(mDownloadFile);
//                        break;
//                    case CANCEL:
//                        mListener.onCancel(mDownloadFile);
//                        break;
//                    case PAUSE:
//                        mListener.onPause(mDownloadFile);
//                        break;
//                    case PUBLISH:
//                        mListener.onPublish(mDownloadFile, Long.parseLong(msg.obj.toString()));
//                        break;
//                    case SUCCESS:
//                        mListener.onSuccess(mDownloadFile);
//                        break;
//                    case START:
//                        mListener.onStart(mDownloadFile, Long.parseLong(msg.obj.toString()));
//                        break;
//                    case GOON:
//                        mListener.onGoon(mDownloadFile, Long.parseLong(msg.obj.toString()));
//                        break;
//                }
//            }
//        };
//
//        // 真正开始下载
//        mThreadPool.execute(new Runnable() {
//            @Override
//            public void run() {
//                download(isGoon, handler);
//            }
//        });
//    }
//
//    /**
//     * 下载方法
//     *
//     * @param handler 消息处理器
//     */
//    private void download(boolean isGoon, Handler handler) {
//        Message msg = null;
//        CommonUtil.LOGD(TAG, "开始下载。。。");
//        try {
//            RandomAccessFile localFile = new RandomAccessFile(new File(mLocalPath + File.separator + mFileName), "rwd");
//
//            DefaultHttpClient client = new DefaultHttpClient();
//            client.setParams(getHttpParams());
//            HttpGet get = new HttpGet(mUrl);
//
//            long localFileLength = getLocalFileLength();
//            final long remoteFileLength = getRemoteFileLength();
//            long downloadedLength = localFileLength;
//
//            // 远程文件不存在
//            if (remoteFileLength == -1l) {
//                CommonUtil.LOGD(TAG, "下载文件不存在...");
//                localFile.close();
//                handler.sendEmptyMessage(ERROR);
//                return;
//            }
//
//            // 本地文件存在
//            if (localFileLength > -1l && localFileLength < remoteFileLength) {
//                CommonUtil.LOGD(TAG, "本地文件存在...");
//                localFile.seek(localFileLength);
//                get.addHeader("Range", "bytes=" + localFileLength + "-" + remoteFileLength);
//            }
//
//            msg = Message.obtain();
//
//            // 如果不是继续下载
//            if (!isGoon) {
//                // 发送开始下载的消息并获取文件大小的消息
//                msg.what = START;
//                msg.obj = remoteFileLength;
//            } else {
//                msg.what = GOON;
//                msg.obj = localFileLength;
//            }
//
//            handler.sendMessage(msg);
//
//            HttpResponse response = client.execute(get);
//            int httpCode = response.getStatusLine().getStatusCode();
//            if (httpCode >= 200 && httpCode <= 300) {
//                InputStream in = response.getEntity().getContent();
//                byte[] bytes = new byte[1024];
//                int len = -1;
//                while (-1 != (len = in.read(bytes))) {
//                    localFile.write(bytes, 0, len);
//                    downloadedLength += len;
//                    //                  Log.log((int)(downloadedLength/(float)remoteFileLength * 100));
//                    if ((int) (downloadedLength / (float) remoteFileLength * 100) % 10 == 0) {
//                        // 发送更新进度的消息
//                        msg = Message.obtain();
//                        msg.what = PUBLISH;
//                        msg.obj = downloadedLength;
//                        handler.sendMessage(msg);
//                        //                      Log.log(mDownloadId + "已下载" + downloadedLength);
//                    }
//
//                    // 暂停下载， 退出方法
//                    if (isPause) {
//                        // 发送暂停的消息
//                        handler.sendEmptyMessage(PAUSE);
//                        CommonUtil.LOGD(TAG, "下载暂停...");
//                        break;
//                    }
//
//                    // 取消下载， 删除文件并退出方法
//                    if (isCanceled) {
//                        CommonUtil.LOGD(TAG, "手动关闭下载。。");
//                        localFile.close();
//                        client.getConnectionManager().shutdown();
//                        new File(mLocalPath + File.separator + mFileName)
//                                .delete();
//                        // 发送取消下载的消息
//                        handler.sendEmptyMessage(CANCEL);
//                        return;
//                    }
//                }
//
//                localFile.close();
//                client.getConnectionManager().shutdown();
//                // 发送下载完毕的消息
//                if (!isPause) handler.sendEmptyMessage(SUCCESS);
//            }
//
//        } catch (Exception e) {
//            // 发送下载错误的消息
//            handler.sendEmptyMessage(ERROR);
//        }
//    }
//
//    /**
//     * 暂停/继续下载
//     * param pause 是否暂停下载
//     * 暂停 return true
//     * 继续 return false
//     */
//    public synchronized boolean pause(boolean pause) {
//        if (!pause) {
//            CommonUtil.LOGD(TAG, "继续下载");
//            isPause = false;
//            start(true); // 开始下载
//        } else {
//            CommonUtil.LOGD(TAG, "暂停下载");
//            isPause = true;
//        }
//        return isPause;
//    }
//
//    /**
//     * 关闭下载， 会删除文件
//     */
//    public synchronized void cancel() {
//        isCanceled = true;
//        if (isPause) {
//            new File(mLocalPath + File.separator + mFileName).delete();
//        }
//    }
//
//    /**
//     * 获取本地文件大小
//     *
//     * @return 本地文件的大小 or 不存在返回-1
//     */
//    public synchronized long getLocalFileLength() {
//        long size = -1l;
//        File localFile = new File(mLocalPath + File.separator + mFileName);
//        if (localFile.exists()) {
//            size = localFile.length();
//        }
//        CommonUtil.LOGD(TAG, "本地文件大小" + size);
//        return size <= 0 ? -1l : size;
//    }
//
//    /**
//     * 获取远程文件打下 or 不存在返回-1
//     *
//     * @return
//     */
//    public synchronized long getRemoteFileLength() {
//        long size = -1l;
//        try {
//            DefaultHttpClient client = new DefaultHttpClient();
//            client.setParams(getHttpParams());
//            HttpGet get = new HttpGet(mUrl);
//
//            HttpResponse response = client.execute(get);
//            int httpCode = response.getStatusLine().getStatusCode();
//            if (httpCode >= 200 && httpCode <= 300) {
//                size = response.getEntity().getContentLength();
//            }
//
//            client.getConnectionManager().shutdown();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        CommonUtil.LOGD(TAG, "远程文件大小" + size);
//        return size;
//    }
//
//    /**
//     * 设置http参数 不能设置soTimeout
//     *
//     * @return HttpParams http参数
//     */
//    private static HttpParams getHttpParams() {
//        HttpParams params = new BasicHttpParams();
//
//        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
//        HttpProtocolParams.setUseExpectContinue(params, true);
//        HttpProtocolParams.setUserAgent(params,
//                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2041.4 Safari/537.36");
//        ConnManagerParams.setTimeout(params, 4000);
//        HttpConnectionParams.setConnectionTimeout(params, 4000);
//
//        return params;
//    }
//
//    /**
//     * 关闭下载线程池
//     */
//    public static void closeDownloadThread() {
//        if (null != mThreadPool) {
//            mThreadPool.shutdownNow();
//        }
//    }
//
//    public interface OnDownloadListener {
//        void onStart(RemoteFileBean downloadFile, long fileSize);  // 回调开始下载
//
//        void onPublish(RemoteFileBean downloadFile, long size);    // 回调更新进度
//
//        void onSuccess(RemoteFileBean downloadFile);               // 回调下载成功
//
//        void onPause(RemoteFileBean downloadFile);                 // 回调暂停
//
//        void onError(RemoteFileBean downloadFile);                 // 回调下载出错
//
//        void onCancel(RemoteFileBean downloadFile);                // 回调取消下载
//
//        void onGoon(RemoteFileBean downloadFile, long localSize);  // 回调继续下载
//    }
//
//
////        Download d = new Download(1, "http://baidu.com/test.zip","/sdcard/download/");
////                d.setOnDownloadListener(new Download.OnDownloadListener() {
////                @Override
////                public void onSuccess(int downloadId) {
////                        System.out.println(downloadId + "下载成功");
////                }
////
////                @Override
////                public void onStart(int downloadId, long fileSize) {
////                        System.out.println(downloadId + "开始下载，文件大小：" + fileSize);
////                }
////
////                @Override
////                public void onPublish(int downloadId, long size) {
////                        System.out.println("更新文件" + downloadId + "大小：" + size);
////                }
////
////                @Override
////                public void onPause(int downloadId) {
////                        System.out.println("暂停下载" + downloadId);
////                }
////
////                @Override
////                public void onGoon(int downloadId, long localSize) {
////                        System.out.println("继续下载" + downloadId);
////                }
////
////                @Override
////                public void onError(int downloadId) {
////                        System.out.println("下载出错" + downloadId);
////                }
////
////                @Override
////                public void onCancel(int downloadId) {
////                        System.out.println("取消下载" + downloadId);
////                }
////        });
////
////                d.start(false);
//}