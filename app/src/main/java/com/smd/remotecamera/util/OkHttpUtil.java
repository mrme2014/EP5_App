package com.smd.remotecamera.util;

import java.net.URL;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class OkHttpUtil {

    private static OkHttpClient mOkHttpClient;

//    private static OkHttpUtil mOkhttpUtil;

    private OkHttpUtil() {
//        mOkHttpClient = new OkHttpClient();
    }

//    public static OkHttpUtil getInstance() {
//        if (mOkhttpUtil == null) {
//            synchronized (OkHttpUtil.class) {
//                if (mOkhttpUtil == null) {
//                    mOkhttpUtil = new OkHttpUtil();
//                }
//            }
//        }
//        return mOkhttpUtil;
//    }

    /**
     * 封装的OkHttp异步Get请求
     *
     * @param url          要访问的URL，可以写URL完整地址，也可以写HOST之后的地址
     * @param parameterMap get请求的参数，将会添加在URL的尾部，若无参数则传递null
     * @param callback     请求成功或失败后的回调，该回调中的两个方法均运行在子线程中
     */
    public static void getAsynchronous(String url, Map<String, String> parameterMap, Callback callback) {
        getAsynchronous(url, parameterMap, null, callback);
    }

    /**
     * 封装的OkHttp异步Get请求
     *
     * @param url          要访问的URL，可以写URL完整地址，也可以写HOST之后的地址
     * @param parameterMap get请求的参数，将会添加在URL的尾部，若无参数则传递null
     * @param headerMap    get请求的头信息，若无头信息则传递null
     * @param callback     请求成功或失败后的回调，该回调中的两个方法均运行在子线程中
     */
    public static void getAsynchronous(String url, Map<String, String> parameterMap, Map<String, String> headerMap, Callback callback) {
        if (callback == null) {
            throw new NullPointerException("回调Callback不能为空");
        }
        if ((parameterMap != null) && (!parameterMap.isEmpty())) {
            Set<String> keySet = parameterMap.keySet();
            boolean first = true;
            for (String key : keySet) {
                if (first) {
                    url = url + "?" + key + "=" + parameterMap.get(key);
                    first = false;
                } else {
                    url = url + "&" + key + "=" + parameterMap.get(key);
                }
            }
        }
        checkInit();
//        url = formatUrl(url);
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.get();
        if ((headerMap != null) && (!headerMap.isEmpty())) {
            Set<String> keySet = headerMap.keySet();
            for (String key : keySet) {
                builder.addHeader(key, headerMap.get(key));
            }
        }
        Request request = builder.build();
        mOkHttpClient.newCall(request).enqueue(callback);
    }

    private static void checkInit() {
        if (mOkHttpClient == null) {
            synchronized (OkHttpUtil.class) {
                if (mOkHttpClient == null) {
                    mOkHttpClient = new OkHttpClient();
                }
            }
        }
    }

    private Call newCall(URL url, long startPoints) {
        Request request = new Request.Builder()
                .url(url)
                .header("RANGE", "bytes=" + startPoints + "-")//断点续传要用到的，指示下载的区间
                .build();
        return mOkHttpClient.newCall(request);
    }
}
