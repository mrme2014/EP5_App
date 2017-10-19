package com.smd.remotecamera.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class CommonUtil {
        @SuppressWarnings("unused")
        private static String TAG = "CommonUtil";

        public static final boolean DEBUG = true;

        private static Toast mToast = null;

        public static void showToast(final Context context, final String msg) {
                runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                                if (mToast == null) {
                                        mToast = Toast.makeText(context, "", Toast.LENGTH_LONG);
                                }
                                mToast.setText(msg);
                                mToast.show();
                        }
                });
        }

        private static Toast mLogToast = null;

        public static void showLogToast(final Context context, final String msg) {
                if (!DEBUG) {
                        return;
                }
                runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                                if (mLogToast == null) {
                                        mLogToast = Toast.makeText(context, "", Toast.LENGTH_LONG);
                                }
                                mLogToast.setText(msg);
                                mLogToast.show();
                        }
                });
        }

        public static void LOGD(String tag, String msg) {
                Log.d(tag, msg);
        }

        public static void LOGE(String tag, String msg) {
                Log.e(tag, msg);
        }

        public static void SYSO(String tag, String msg) {
                System.out.println(tag + ": " + msg);
        }

        private static Handler mHandler = new Handler(Looper.getMainLooper());

        public static void runOnUIThread(Runnable runnable) {
                mHandler.post(runnable);
        }

        /**
         * 获取内置SD卡路径
         *
         * @return
         */
        public static String getInnerSDCardPath() {
                return Environment.getExternalStorageDirectory().getPath();
        }

        /**
         * 获取外置SD卡路径
         *
         * @return 应该就一条记录或空
         */
        public static List<String> getExtSDCardPath() {
                List<String> resultList = new ArrayList<>();
                try {
                        Runtime runtime = Runtime.getRuntime();
                        Process proc = runtime.exec("mount");
                        InputStream is = proc.getInputStream();
                        InputStreamReader isr = new InputStreamReader(is);
                        BufferedReader br = new BufferedReader(isr);
                        String lineStr;
                        while ((lineStr = br.readLine()) != null) {
                                // 将常见的linux分区过滤掉
                                if (lineStr.contains("secure"))
                                        continue;
                                if (lineStr.contains("asec"))
                                        continue;
                                if (lineStr.contains("media"))
                                        continue;
                                if (lineStr.contains("system") || lineStr.contains("cache")
                                        || lineStr.contains("sys") || lineStr.contains("data")
                                        || lineStr.contains("tmpfs")
                                        || lineStr.contains("shell")
                                        || lineStr.contains("root") || lineStr.contains("acct")
                                        || lineStr.contains("proc") || lineStr.contains("misc")
                                        || lineStr.contains("obb")) {
                                        continue;
                                }

                                if (lineStr.contains("fat") || lineStr.contains("fuse")
                                        || (lineStr.contains("ntfs"))) {

                                        String columns[] = lineStr.split(" ");
                                        if (columns != null && columns.length > 1) {
                                                String path = columns[1];
                                                if (path != null && !resultList.contains(path)
                                                        && path.contains("sd"))
                                                        resultList.add(columns[1]);
                                        }
                                }
                        }
                        br.close();
                } catch (Exception e) {
                        e.printStackTrace();
                }
                String innerPath = getInnerSDCardPath();
                if (resultList.contains(innerPath)) {
                        resultList.remove(innerPath);
                }
                return resultList;
        }


        /**
         * 需要权限： <uses-permission
         * android:name="android.permission.ACCESS_NETWORK_STATE"/>
         *
         * @param context
         * @return
         */
        public static boolean isNetworkAvailable(Context context) {
                // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
                ConnectivityManager connectivityManager = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                if (connectivityManager == null) {
                        return false;
                } else {
                        // 获取NetworkInfo对象
                        NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
                        if (networkInfo != null && networkInfo.length > 0) {
                                for (int i = 0; i < networkInfo.length; i++) {
                                        System.out.println(i + "===状态==="
                                                + networkInfo[i].getState());
                                        System.out.println(i + "===类型==="
                                                + networkInfo[i].getTypeName());
                                        // 判断当前网络状态是否为连接状态
                                        if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
                                                return true;
                                        }
                                }
                        }
                }
                return false;
        }

        /**
         * 获取本机IMEI
         *
         * @param context
         * @return
         */
        public static String getIMEI(Context context) {
                TelephonyManager tm = (TelephonyManager) context
                        .getSystemService(Context.TELEPHONY_SERVICE);
                return tm.getDeviceId();
        }

        public static void showSingleToast(final Context mContext, final String msg) {
                runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                                Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                        }
                });
        }

        /**
         * 将一个字符串转成16进制数组成的字符串
         *
         * @return
         */
        public static String encode2Hex(String str) {
                String hexString = "0123456789ABCDEF";
                byte[] strBytes = str.getBytes();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < str.length(); i++) {
                        sb.append(hexString.charAt(strBytes[i] >> 4));
                        sb.append(hexString.charAt(strBytes[i] & 0x0F));
                }
                return sb.toString();
        }

        /**
         * 生成16位由十六进制数组成的随机字符串
         *
         * @return
         */
        public static String createRandomStr() {
                String hexString = "0123456789ABCDEF";
                StringBuilder sb = new StringBuilder();
                Random random = new Random();
                for (int i = 0; i < 16; i++) {
                        sb.append(hexString.charAt(random.nextInt(16)));
                }
                return sb.toString();
        }

        /**
         * 解密文件
         *
         * @param context   上下文
         * @param file      要解密的文件
         * @param secretKey 密钥
         * @param interval  解密间隔
         * @return 解密后的文件
         */
        public static File decodeFile(Context context, File file, byte secretKey,
                                      int interval) {
                File resultFile = new File(context.getCacheDir().getAbsolutePath()
                        + File.separator + context.getPackageName());
                FileInputStream fis = null;
                FileOutputStream fos = null;
                try {
                        if (resultFile.exists()) {
                                resultFile.delete();
                                resultFile.createNewFile();
                        }
                        fis = new FileInputStream(file);
                        fos = new FileOutputStream(resultFile);
                        byte[] buff = new byte[1024];
                        byte[] result;
                        int count;
                        while ((count = fis.read(buff)) > 0) {
                                result = decode(buff, count, secretKey, interval);
                                fos.write(result);
                        }
                } catch (IOException e) {
                        e.printStackTrace();
                } finally {
                        try {
                                if (fis != null)
                                        fis.close();
                                if (fos != null)
                                        fos.close();
                        } catch (IOException e) {
                                e.printStackTrace();
                        }
                }
                return resultFile;
        }

        /**
         * 将一个数组内容按照指定方式进行解密
         *
         * @param data      要解密的数组
         * @param count     数组中实际的有效长度
         * @param secretKey 密钥
         * @param interval  解密间隔
         * @return 解码后的数组，该数组长度等于源数据中的有效长度
         */
        private static byte[] decode(byte[] data, int count, byte secretKey,
                                     int interval) {
                byte[] result = Arrays.copyOf(data, count);
                for (int i = 0; i < count; i = i + interval) {
                        result[i] = (byte) (result[i] ^ secretKey);
                }
                return result;
        }
}
