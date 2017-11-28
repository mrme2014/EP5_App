package com.smd.remotecamera.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ntk.nvtkit.NVTKitModel;
import com.qiaomu.libvideo.utils.AppUtils;
import com.smd.remotecamera.R;
import com.smd.remotecamera.controller.CameraController;
import com.smd.remotecamera.service.WifiConnectBroadcastReciver;
import com.smd.remotecamera.service.WifiConnectInterface;
import com.smd.remotecamera.util.CommonUtil;
import com.smd.remotecamera.util.Util;

import org.videolan.libvlc.VideoInterface;

import jczj.android.com.sharelib.ShareUtil;

public class CameraActivity extends AppCompatActivity implements VideoInterface, View.OnClickListener {
    private static final int REQUEST_PERMISSION_STORAGE = 100;

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private ImageView mIbPhoto;
    private ImageView mIbVideo;
    private FrameLayout mIbEdit;
    private FrameLayout mIbDownload;
    private View bottomLayout;
    private LinearLayout emptylayout;
    private TextView mTvREC;
    private ImageView imageView;
    private ProgressDialog mProgressDialog;

    private static int mCameraWidth;
    private static int mCameraHeight;

    private boolean mCheckSize = true;
    private boolean mIsVideo = false;
    private boolean mNeedTakePhoto = false;
    private boolean mIsConnected = false;

    private CameraController mCameraController;
    private WifiConnectBroadcastReciver mBroadcastReciver;

    private static Handler mVideoHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_new);

        initView();
        init();
        requestpermissions();
        reigisterBroadcastReciver();
        ShareUtil.registWx(this);

    }


    private void reigisterBroadcastReciver() {

        mBroadcastReciver = new WifiConnectBroadcastReciver(new WifiConnectInterface() {
            @Override
            public void onConnected() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        postDelayChangedMode();
                    }
                }, 1500);
                mIsConnected = true;
            }

            @Override
            public void onDisConnected() {
                mIsConnected = false;
            }
        });
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        filter.setPriority(1000);
        registerReceiver(mBroadcastReciver, filter);
    }

    private void requestpermissions() {
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_STORAGE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                AppUtils.showAlertDialog(this, R.string.permission_storage_message,
                        R.string.go_grant, R.string.exit_app, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == -1)
                                    AppUtils.startAppDetailSetting(CameraActivity.this);
                                else
                                    System.exit(0);
                            }
                        });
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        postDelayChangedMode();
    }

    private void postDelayChangedMode() {
        mCameraController.changeMode(NVTKitModel.MODE_MOVIE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mCheckSize) {
                    if ((mCameraWidth != 0) && (mCameraHeight != 0)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                resizeSurfaceView();
                            }
                        });
                        break;
                    }
                    SystemClock.sleep(500);
                }
            }
        }).start();
    }

    private void resizeSurfaceView() {
        if (mCameraWidth == 0 || mCameraHeight == 0)
            return;
        ViewGroup.LayoutParams lp = mSurfaceView.getLayoutParams();
        Pair<Integer, Integer> screenSize = Util.getScreenSize(CameraActivity.this);
        lp.width = screenSize.first;
        lp.height = screenSize.first / mCameraWidth * mCameraHeight;
        mSurfaceView.setLayoutParams(lp);
        mSurfaceView.invalidate();
    }

    @Override
    protected void onDestroy() {
        if (mIsVideo) {
            mIbVideo.performClick();
        }
        mCheckSize = false;
        unregisterReceiver(mBroadcastReciver);
        super.onDestroy();
    }

    private void init() {
        mCameraController = new CameraController(this, this, mVideoHandler, mSurfaceHolder, mSurfaceView);
        mCameraController.setOnCameraControllerListener(mOnCameraControllerListener);
    }

    private void initView() {
        mSurfaceView = (SurfaceView) findViewById(R.id.camera_sv);
        mSurfaceHolder = mSurfaceView.getHolder();
        mIbPhoto = (ImageView) findViewById(R.id.camera_ib_camera);
        mIbVideo = (ImageView) findViewById(R.id.recoder);
        mIbEdit = (FrameLayout) findViewById(R.id.camera_ib_device);
        mIbDownload = (FrameLayout) findViewById(R.id.camera_ib_download);
        mTvREC = (TextView) findViewById(R.id.camera_tv_isvideo);
        bottomLayout = findViewById(R.id.camear_ll_bottom);
        emptylayout = (LinearLayout) findViewById(R.id.empty_layout);
        mIbPhoto.setOnClickListener(this);
        mIbVideo.setOnClickListener(this);
        mIbEdit.setOnClickListener(this);
        mIbDownload.setOnClickListener(this);
        findViewById(R.id.emptyTipTv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("android.net.wifi.PICK_WIFI_NETWORK");
                startActivity(intent);
            }
        });
        findViewById(R.id.empty_layout).setBackgroundColor(Color.BLACK);
        ((TextView) findViewById(R.id.emptyTipTv)).setText("点我去链接设备~");
        ((TextView) findViewById(R.id.emptyTipTv)).setTextColor(Color.WHITE);

        imageView = (ImageView) findViewById(R.id.settingIv);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CameraActivity.this, MenuActivity.class));
            }
        });
    }


    private void showTopPb() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("请稍后...");
        }
        mProgressDialog.show();
    }

    private void hideTopPb() {
        mProgressDialog.dismiss();
    }

    @Override
    public void setSize(int width, int height) {
        if (width == 0 || height == 0)
            return;
        mCameraWidth = width;
        mCameraHeight = height;
        resizeSurfaceView();
    }


    @Override
    public void onClick(View v) {


        switch (v.getId()) {
            case R.id.recoder:
                if (!mIsConnected) {
                    CommonUtil.showToast(this, getString(R.string.no_devices));
                    return;
                }
                if (mIsVideo) {
                    mCameraController.stopVideo();
                    showTopPb();
                } else {
                    mCameraController.startVideo();
                    showTopPb();
                }
                break;
            case R.id.camera_ib_camera:
                if (!mIsConnected) {
                    CommonUtil.showToast(this, getString(R.string.no_devices));
                    return;
                }
                if (mIsVideo) {
                    mIbVideo.performClick();
                    mNeedTakePhoto = true;
                    break;
                }
                mCameraController.takePhoto();
                showTopPb();
                break;
            case R.id.camera_ib_device:
                startActivity(new Intent(this, DownloadListActivity.class));
                break;
            case R.id.camera_ib_download:
                startActivity(new Intent(this, EditListActivity.class));
                break;
        }
    }


    private CameraController.OnCameraControllerListener mOnCameraControllerListener = new CameraController.OnCameraControllerListener() {
        @Override
        public void onModeChanged(int mode) {
            emptylayout.setVisibility(View.GONE);
        }

        @Override
        public void onTakePhoto(boolean success, String photoName, String photoPath, int freePicNum) {
            hideTopPb();
            CommonUtil.showToast(CameraActivity.this, success ? "拍照成功" : "拍照失败");
        }

        @Override
        public void onStartVideo(boolean success) {
            mIsVideo = success;
            hideTopPb();
            if (success) {
                mHandler.post(mRECRunnable);
            }
            CommonUtil.showToast(CameraActivity.this, success ? "开始录像成功" : "开始录像失败");
        }

        @Override
        public void onStopVideo(boolean success) {
            mIsVideo = !success;
            hideTopPb();
            if (success) {
                mHandler.removeCallbacks(mRECRunnable);
                mTvREC.setVisibility(View.INVISIBLE);
                if (mNeedTakePhoto) {
                    mNeedTakePhoto = false;
                    mIbPhoto.performClick();
                }
            }
            CommonUtil.showToast(CameraActivity.this, success ? "停止录像成功" : "停止录像失败");
        }

        @Override
        public void onChangedFailed() {
            // emptylayout.setVisibility(View.VISIBLE);
        }
    };

    private Runnable mRECRunnable = new Runnable() {
        @Override
        public void run() {
            mTvREC.setVisibility((mTvREC.getVisibility() == View.VISIBLE) ? View.INVISIBLE : View.VISIBLE);
            mHandler.postDelayed(this, 1000);
        }
    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mIbEdit.setVisibility(View.VISIBLE);
            mIbDownload.setVisibility(View.VISIBLE);
            resizeSurfaceView();
        } else {
            mIbEdit.setVisibility(View.GONE);
            mIbDownload.setVisibility(View.GONE);
            resizeSurfaceView();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

    }
}
