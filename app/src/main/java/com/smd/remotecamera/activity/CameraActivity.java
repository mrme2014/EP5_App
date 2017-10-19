package com.smd.remotecamera.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Pair;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ntk.nvtkit.NVTKitModel;
import com.smd.remotecamera.R;
import com.smd.remotecamera.controller.CameraController;
import com.smd.remotecamera.util.CommonUtil;
import com.smd.remotecamera.util.Util;
import com.smd.remotecamera.view.InterceptTouchLayout;

import org.videolan.libvlc.VideoInterface;

public class CameraActivity extends Activity implements VideoInterface, View.OnClickListener {

        private SurfaceView mSurfaceView;
        private SurfaceHolder mSurfaceHolder;
        private ImageButton mIbPhoto;
        private ImageButton mIbVideo;
        private ImageButton mIbEdit;
        private ImageButton mIbDownload;
        private InterceptTouchLayout mPbTop;
        private TextView mTvREC;

        private static int mCameraWidth;
        private static int mCameraHeight;

        private boolean mCheckSize = true;
        private boolean mIsVideo = false;
        private boolean mNeedTakePhoto = false;

        private CameraController mCameraController;

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
                setContentView(R.layout.activity_camera);

                initView();
                init();
        }

        @Override
        protected void onResume() {
                super.onResume();
                mCameraController.changeMode(NVTKitModel.MODE_PHOTO);

                new Thread(new Runnable() {
                        @Override
                        public void run() {
                                while (mCheckSize) {
                                        if ((mCameraWidth != 0) && (mCameraHeight != 0)) {
                                                runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                                ViewGroup.LayoutParams lp = mSurfaceView.getLayoutParams();
                                                                Pair<Integer, Integer> screenSize = Util.getScreenSize(CameraActivity.this);
                                                                lp.width = screenSize.first;
                                                                lp.height = screenSize.first / mCameraWidth * mCameraHeight;
                                                                mSurfaceView.setLayoutParams(lp);
                                                                mSurfaceView.invalidate();
                                                        }
                                                });
                                                break;
                                        }
                                        SystemClock.sleep(500);
                                }
                        }
                }).start();
        }

        @Override
        protected void onDestroy() {
                if (mIsVideo) {
                        mIbVideo.performClick();
                }
                mCheckSize = false;
                super.onDestroy();
        }

        private void init() {
                mCameraController = new CameraController(this, this, mVideoHandler, mSurfaceHolder, mSurfaceView);
                mCameraController.setOnCameraControllerListener(mOnCameraControllerListener);
        }

        private void initView() {
                mSurfaceView = (SurfaceView) findViewById(R.id.camera_sv);
                mSurfaceHolder = mSurfaceView.getHolder();
                mIbPhoto = (ImageButton) findViewById(R.id.camera_ib_camera);
                mIbVideo = (ImageButton) findViewById(R.id.camera_ib_video);
                mIbEdit = (ImageButton) findViewById(R.id.camera_ib_edit);
                mIbDownload = (ImageButton) findViewById(R.id.camera_ib_download);
                mPbTop = (InterceptTouchLayout) findViewById(R.id.camera_pb_top);
                mTvREC = (TextView) findViewById(R.id.camera_tv_isvideo);
                mIbPhoto.setOnClickListener(this);
                mIbVideo.setOnClickListener(this);
                mIbEdit.setOnClickListener(this);
                mIbDownload.setOnClickListener(this);
        }


//        private void init() {
//                new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                                Map result = NVTKitModel.qryDeviceStatus();
//                                Iterator iter = result.entrySet().iterator();
//                                while (iter.hasNext()) {
//                                        Map.Entry entry = (Map.Entry) iter.next();
//                                        String key = (String) entry.getKey();
//                                        final String val = (String) entry.getValue();
//                                        switch (key) {
//                                                case DefineTable.WIFIAPP_CMD_CAPTURESIZE:
//                                                        NVTKitModel.setPhotoSize("4");
//                                                        runOnUiThread(new Runnable() {
//                                                                @Override
//                                                                public void run() {
////                                    Toast.makeText(CameraActivity.this, val, Toast.LENGTH_SHORT).show();
//                                                                }
//                                                        });
//                                                case DefineTable.WIFIAPP_CMD_MOVIE_REC_SIZE:
//                                                case DefineTable.WIFIAPP_CMD_CYCLIC_REC:
//                                                case DefineTable.WIFIAPP_CMD_MOVIE_HDR:
//                                                case DefineTable.WIFIAPP_CMD_MOTION_DET:
//                                                case DefineTable.WIFIAPP_CMD_MOVIE_AUDIO:
//                                                case DefineTable.WIFIAPP_CMD_DATEIMPRINT:
//                                                case DefineTable.WIFIAPP_CMD_MOVIE_GSENSOR_SENS:
//                                                case DefineTable.WIFIAPP_CMD_SET_AUTO_RECORDING:
//                                                case DefineTable.WIFIAPP_CMD_POWEROFF:
//                                                case DefineTable.WIFIAPP_CMD_TVFORMAT:
//                                        }
//                                }
//                                ;
//                        }
//                }).start();
//        }

        private void showTopPb() {
                mPbTop.setVisibility(View.VISIBLE);
        }

        private void hideTopPb() {
                mPbTop.setVisibility(View.GONE);
        }

        @Override
        public void setSize(int width, int height) {
                mCameraWidth = width;
                mCameraHeight = height;
                CommonUtil.SYSO("remotecameraactivity", "setSize: mCameraWidth :" + mCameraWidth + " ; mCameraHeight :" + mCameraHeight);
                ViewGroup.LayoutParams lp = mSurfaceView.getLayoutParams();
                Pair<Integer, Integer> screenSize = Util.getScreenSize(CameraActivity.this);
                lp.width = screenSize.first;
                lp.height = screenSize.first / mCameraWidth * mCameraHeight;
                mSurfaceView.setLayoutParams(lp);
                mSurfaceView.invalidate();
        }

        @Override
        public void onClick(View v) {
                switch (v.getId()) {
                        case R.id.camera_ib_video:
                                if (mIsVideo) {
                                        mCameraController.stopVideo();
                                        showTopPb();
                                } else {
                                        mCameraController.startVideo();
                                        showTopPb();
                                }
                                break;
                        case R.id.camera_ib_camera:
                                if (mIsVideo) {
                                        mIbVideo.performClick();
                                        mNeedTakePhoto = true;
                                        break;
                                }
                                mCameraController.takePhoto();
                                showTopPb();
                                break;
                        case R.id.camera_ib_edit:
                                startActivity(new Intent(this, EditListActivity.class));
                                break;
                        case R.id.camera_ib_download:
                                startActivity(new Intent(this, DownloadListActivity.class));
                                break;
                }
        }

        private CameraController.OnCameraControllerListener mOnCameraControllerListener = new CameraController.OnCameraControllerListener() {
                @Override
                public void onModeChanged(int mode) {

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
        };

        private Runnable mRECRunnable = new Runnable() {
                @Override
                public void run() {
                        mTvREC.setVisibility((mTvREC.getVisibility() == View.VISIBLE) ? View.INVISIBLE : View.VISIBLE);
                        mHandler.postDelayed(this, 1000);
                }
        };
}
