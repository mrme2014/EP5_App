//package com.smd.remotecamera.util;
//
//import java.util.Map;
//import java.util.Set;
//
//import okhttp3.Callback;
//import okhttp3.FormBody;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//
///**
// * 网络操作工具类
// */
//public abstract class NetUtils {
//    private static OkHttpClient okHttpClient;
//
//    private NetUtils() {
//    }
//
//    /**
//     * 获取一个OkHttpClient对象，该对象在本应用中只存在一个。
//     *
//     * @return 应用中唯一的OkHttpClient对象
//     */
//    public static OkHttpClient getOkHttpClient() {
//        if (okHttpClient == null) {
//            okHttpClient = new OkHttpClient();
//        }
//        return okHttpClient;
//    }
//
//    /**
//     * 封装的OkHttp异步Get请求
//     *
//     * @param url          要访问的URL，可以写URL完整地址，也可以写HOST之后的地址
//     * @param parameterMap get请求的参数，将会添加在URL的尾部，若无参数则传递null
//     * @param callback     请求成功或失败后的回调，该回调中的两个方法均运行在子线程中
//     */
//    public static void getAsynchronous(String url, Map<String, String> parameterMap, Callback callback) {
//        getAsynchronous(url, parameterMap, null, callback);
//    }
//
//    /**
//     * 封装的OkHttp异步Get请求
//     *
//     * @param url          要访问的URL，可以写URL完整地址，也可以写HOST之后的地址
//     * @param parameterMap get请求的参数，将会添加在URL的尾部，若无参数则传递null
//     * @param headerMap    get请求的头信息，若无头信息则传递null
//     * @param callback     请求成功或失败后的回调，该回调中的两个方法均运行在子线程中
//     */
//    public static void getAsynchronous(String url, Map<String, String> parameterMap, Map<String, String> headerMap, Callback callback) {
//        if (callback == null) {
//            throw new NullPointerException("回调Callback不能为空");
//        }
//        if ((parameterMap != null) && (!parameterMap.isEmpty())) {
//            Set<String> keySet = parameterMap.keySet();
//            boolean first = true;
//            for (String key : keySet) {
//                if (first) {
//                    url = url + "?" + key + "=" + parameterMap.get(key);
//                    first = false;
//                } else {
//                    url = url + "&" + key + "="  + parameterMap.get(key);
//                }
//            }
//        }
//        checkInit();
//        url = formatUrl(url);
//        Request.Builder builder = new Request.Builder();
//        builder.url(url);
//        builder.get();
//        if ((headerMap != null) && (!headerMap.isEmpty())) {
//            Set<String> keySet = headerMap.keySet();
//            for (String key : keySet) {
//                builder.addHeader(key, headerMap.get(key));
//            }
//        }
//        Request request = builder.build();
//        okHttpClient.newCall(request).enqueue(callback);
//    }
//
//    /**
//     * 封装的OkHttp异步Post请求
//     *
//     * @param url              要访问的URL，可以写URL完整地址，也可以写HOST之后的地址
//     * @param bodyParameterMap post请求的请求体参数，将会添加在请求体中，若无参数则传递null
//     * @param callback         请求成功或失败后的回调，该回调中的两个方法均运行在子线程中
//     */
//    public static void postAsynchronous(String url, Map<String, String> bodyParameterMap, Callback callback) {
//        postAsynchronous(url, null, bodyParameterMap, callback);
//    }
//
//    /**
//     * 封装的OkHttp异步Post请求
//     *
//     * @param url              要访问的URL，可以写URL完整地址，也可以写HOST之后的地址
//     * @param headerMap        post请求的请求头参数，将会添加在请求头中，若无参数则传递null
//     * @param bodyParameterMap post请求的请求体参数，将会添加在请求体中，若无参数则传递null
//     * @param callback         请求成功或失败后的回调，该回调中的两个方法均运行在子线程中
//     */
//    public static void postAsynchronous(String url, Map<String, String> headerMap, Map<String, String> bodyParameterMap, Callback callback) {
//        if (callback == null) {
//            throw new NullPointerException("回调Callback不能为空");
//        }
//        checkInit();
//        url = formatUrl(url);
//        FormBody.Builder formBodyBuilder = new FormBody.Builder();
//        if ((bodyParameterMap != null) && (!bodyParameterMap.isEmpty())) {
//            Set<String> keySet = bodyParameterMap.keySet();
//            for (String key : keySet) {
//                formBodyBuilder.add(key, bodyParameterMap.get(key));
//            }
//        }
//        FormBody formBody = formBodyBuilder.build();
//        Request.Builder requestBuilder = new Request.Builder();
//        requestBuilder.url(url);
//        if ((headerMap != null) && (!headerMap.isEmpty())) {
//            Set<String> keySet = headerMap.keySet();
//            for (String key : keySet) {
//                requestBuilder.addHeader(key, headerMap.get(key));
//            }
//        }
//        requestBuilder.post(formBody);
//        Request request = requestBuilder.build();
//        okHttpClient.newCall(request).enqueue(callback);
//    }
//
//    /**
//     * 格式化URL字符串，若传递的为完整的URL字符串，则直接返回，若不完整，则在URL前边添加HOST后返回
//     *
//     * @param url 要进行格式化的URL字符串
//     * @return 格式化后的URL字符串，该字符串为完整的URL
//     */
//    public static String formatUrl(String url) {
//        if (url == null) {
//            throw new NullPointerException("URL不能为空");
//        }
//        return url;
////        if (url.startsWith("http")) {
////            return url;
////        } else if (url.startsWith("/")) {
////            return NetConstants.HOST + url;
////        } else {
////            return NetConstants.HOST + "/" + url;
////        }
//    }
//
//    private static void checkInit() {
//        if (okHttpClient == null) {
//            okHttpClient = new OkHttpClient();
//        }
//    }
//
//}
