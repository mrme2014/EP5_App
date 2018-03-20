package com.smd.remotecamera.controller;


import android.content.Context;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.ntk.nvtkit.NVTKitModel;
import com.smd.remotecamera.constants.RemoteCameraConstants;
import com.smd.remotecamera.util.CommonUtil;

import org.videolan.libvlc.VideoInterface;

import java.util.Map;

public class CameraController {
    private Context mContext;
    private VideoInterface mVideoInterface;
    private Handler mHandler;
    private SurfaceHolder mSurfaceHolder;
    private SurfaceView mSurfaceView;

    private OnCameraControllerListener mOnCameraChangedListener;
    private int mCurrentMode;

    public CameraController(Context context, VideoInterface videoInterface, Handler handler, SurfaceHolder surfaceHolder, SurfaceView surfaceView) {
        mContext = context;
        mVideoInterface = videoInterface;
        mHandler = handler;
        mSurfaceHolder = surfaceHolder;
        mSurfaceView = surfaceView;
    }

    public void changeMode(int mode) {
        changeMode(mode, false);
    }

    private void changeMode(final int mode, final boolean callInnerListener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String changeModeResult = NVTKitModel.changeMode(mode);
                    if (changeModeResult != null) {
                       // Thread.sleep(1000);
                        if (mode == NVTKitModel.MODE_PHOTO) {
                            try {
                                NVTKitModel.videoPlayForPhotoCapture(mContext, mVideoInterface, mHandler, mSurfaceHolder, mSurfaceView);
                            } catch (Exception e) {
                            }catch (Throwable e){}
                        } else if (mode == NVTKitModel.MODE_MOVIE) {
                            try {
                                NVTKitModel.videoPlayForLiveView(mContext, mVideoInterface, mHandler, mSurfaceHolder, mSurfaceView);
                            } catch (Exception e) {
                            }catch (Throwable e){}
                        }
                    } else {
                        CommonUtil.showToast(mContext, "changeMode fail!!!");
                        return;
                    }
                    final String autoTestDoneResult = NVTKitModel.autoTestDone();
                    if ((autoTestDoneResult != null) && mOnCameraChangedListener != null) {
                        mCurrentMode = mode;
                        CommonUtil.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                mOnCameraChangedListener.onModeChanged(mode);
                            }
                        });
                    }
                    if (callInnerListener) {
                        onModeChanged(mode);
                    }
                } catch (Exception e) {

                }catch (Throwable e){}
            }
        }).start();
    }

    public void takePhoto() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mCurrentMode != NVTKitModel.MODE_PHOTO) {
                    changeMode(NVTKitModel.MODE_PHOTO, true);
                    return;
                }
                takePhotoNoModeCheck();
            }
        }).start();
    }

    private void takePhotoNoModeCheck() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Map finalResult = NVTKitModel.takePhoto();
                    CommonUtil.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mOnCameraChangedListener == null) {
                                return;
                            }
                            if (finalResult == null) {
                                mOnCameraChangedListener.onTakePhoto(false, null, null, -1);
                            } else {
                                mOnCameraChangedListener.onTakePhoto(true,
                                        finalResult.get(RemoteCameraConstants.RESULT_TAKEPHOTO_PHOTONAME).toString(),
                                        finalResult.get(RemoteCameraConstants.RESULT_TAKEPHOTO_PHOTOPATH).toString(),
                                        Integer.valueOf(finalResult.get(RemoteCameraConstants.RESULT_TAKEPHOTO_FREE_NUM).toString()));
                            }
                        }
                    });
                } catch (Exception e) {
                }catch (Throwable e){}
            }
        }).start();
    }

    public void startVideo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mCurrentMode != NVTKitModel.MODE_MOVIE) {
                    try {
                        changeMode(NVTKitModel.MODE_MOVIE, true);
                    } catch (Exception e) {
                    }catch (Throwable e){}
                    return;
                }
                startVideoNoModeCheck();
            }
        }).start();
    }

    private void startVideoNoModeCheck() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String result = NVTKitModel.recordStart();
                    CommonUtil.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mOnCameraChangedListener == null) {
                                return;
                            }
                            mOnCameraChangedListener.onStartVideo(RemoteCameraConstants.RESULT_SUCCESS.equals(result));
                        }
                    });
                } catch (Exception e) {
                }catch (Throwable e){}
                try {
                    NVTKitModel.videoPlayForLiveView(mContext, mVideoInterface, mHandler, mSurfaceHolder, mSurfaceView);
                    final String ack3 = NVTKitModel.autoTestDone();
                } catch (Exception e) {
                }catch (Throwable e){}
            }
        }).start();
    }


    public void stopVideo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String result = NVTKitModel.recordStop();
                    CommonUtil.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            mOnCameraChangedListener.onStopVideo(RemoteCameraConstants.RESULT_SUCCESS.equals(result));
                        }
                    });
                    NVTKitModel.videoPlayForLiveView(mContext, mVideoInterface, mHandler, mSurfaceHolder, mSurfaceView);
                    final String ack3 = NVTKitModel.autoTestDone();
                } catch (Exception e) {
                }catch (Throwable e){}
            }
        }).start();
    }

    private void onModeChanged(int mode) {
        if (mode == NVTKitModel.MODE_PHOTO) {
            takePhotoNoModeCheck();
        } else if (mode == NVTKitModel.MODE_MOVIE) {
            startVideoNoModeCheck();
        }
    }

    public void setOnCameraControllerListener(OnCameraControllerListener onCameraControllerListener) {
        mOnCameraChangedListener = onCameraControllerListener;
    }

    public interface OnCameraControllerListener {
        void onModeChanged(int mode);

        void onTakePhoto(boolean success, String photoName, String photoPath, int freePicNum);

        void onStartVideo(boolean success);

        void onStopVideo(boolean success);

        void onChangedFailed();
    }

}
