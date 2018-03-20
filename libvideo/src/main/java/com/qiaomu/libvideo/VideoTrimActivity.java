package com.qiaomu.libvideo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.qiaomu.libvideo.utils.AppUtils;
import com.qiaomu.libvideo.utils.Config;
import com.qiaomu.libvideo.utils.ToastUtils;
import com.qiaomu.libvideo.view.CustomProgressDialog;
import com.qiaomu.libvideo.view.ThumbnailView;
import com.qiniu.pili.droid.shortvideo.PLShortVideoTrimmer;
import com.qiniu.pili.droid.shortvideo.PLVideoFrame;
import com.qiniu.pili.droid.shortvideo.PLVideoSaveListener;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import jczj.android.com.sharelib.ShareDialog;
import okhttp3.internal.Util;

public class VideoTrimActivity extends AppCompatActivity {
    private static final String TAG = "VideoTrimActivity";

    private static final int SLICE_COUNT = 8;

    private PLShortVideoTrimmer mShortVideoTrimmer;

    private LinearLayout mFrameListView;
    private View mHandlerLeft;
    private View mHandlerRight;
    private ThumbnailView mThumbnailView;

    private CustomProgressDialog mProcessingDialog;
    private VideoView mPreview;

    private long mSelectedBeginMs;
    private long mSelectedEndMs;
    private long mDurationMs;

    private int mVideoFrameCount;
    private int mSlicesTotalLength;

    private Handler mHandler = new Handler();
    private TextView duration;
    private String file_path;
    private String trimed_path;
    private MediaScannerConnection msc;
    private boolean isTrimOk;
    private ImageView controllIv;
    private int currentPosition;
    private boolean trim;

    public static void startTrimActivity(Context from, String filePath) {
        startTrimActivity(from, filePath, true);
    }

    public static void startTrimActivity(Context from, String filePath, boolean trim) {
        Intent intent = new Intent(from, VideoTrimActivity.class);
        intent.putExtra("file_path", filePath);
        intent.putExtra("trim", trim);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mProcessingDialog = new CustomProgressDialog(this);
        mProcessingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mShortVideoTrimmer.cancelTrim();
            }
        });


        file_path = getIntent().getStringExtra("file_path");
        trim = getIntent().getBooleanExtra("trim", true);
        init(file_path);
    }

    @Override
    protected void onResume() {
        super.onResume();
        play();
        onShare();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTrackPlayProgress();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mShortVideoTrimmer != null) {
            mShortVideoTrimmer.destroy();
            mPreview.stopPlayback();
            stopTrackPlayProgress();
        }
    }

    private void startTrackPlayProgress() {
        stopTrackPlayProgress();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mPreview.getCurrentPosition() >= mSelectedEndMs) {
                    mPreview.seekTo((int) mSelectedBeginMs);
                }
                mHandler.postDelayed(this, 100);
            }
        }, 100);
    }

    private void stopTrackPlayProgress() {
        mHandler.removeCallbacksAndMessages(null);
    }

    private void play() {
        if (mPreview != null) {
            mPreview.seekTo((int) mSelectedBeginMs);
            mPreview.start();
            startTrackPlayProgress();
        }
    }

    private void init(String videoPath) {
        setContentView(R.layout.activity_trim);
        controllIv = (ImageView) findViewById(R.id.controllIv);
        findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDone();
            }
        });

        duration = (TextView) findViewById(R.id.duration);
        mPreview = (VideoView) findViewById(R.id.preview);

        mShortVideoTrimmer = new PLShortVideoTrimmer(this, videoPath, Config.TRIM_FILE_PATH);
        mThumbnailView = (ThumbnailView) findViewById(R.id.thumbnailView);
        mThumbnailView.setMinInterval(1);
        mThumbnailView.setOnScrollBorderListener(new ThumbnailView.OnScrollBorderListener() {
            @Override
            public void OnScrollBorder(float start, float end) {
                changeTime();
            }

            @Override
            public void onScrollStateChange() {

            }
        });

        initPreview(videoPath);

        if (!trim) {
            TextView title = (TextView) findViewById(R.id.title);
            title.setText("视频预览");
            findViewById(R.id.done).setVisibility(View.GONE);
            findViewById(R.id.trim_tip).setVisibility(View.GONE);
            findViewById(R.id.video_frame_layout).setVisibility(View.GONE);
            findViewById(R.id.duration_layout).setVisibility(View.GONE);
        }
    }

    private void changeTime() {

        float left = mThumbnailView.getLeftInterval();
        float beginPercent = left / mThumbnailView.getWidth();

        mSelectedBeginMs = (int) (mDurationMs * beginPercent);

        float right = mThumbnailView.getRightInterval();
        float endPercent = right / mThumbnailView.getWidth();
        mSelectedEndMs = (int) (mDurationMs * endPercent);

        beginPercent = clamp(beginPercent);
        endPercent = clamp(endPercent);
        mSelectedBeginMs = (long) (beginPercent * mDurationMs);
        mSelectedEndMs = (long) (endPercent * mDurationMs);
        updateRangeText();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initPreview(String videoPath) {

        try {
            mSelectedEndMs = mDurationMs = mShortVideoTrimmer.getSrcDurationMs();
            duration.setText("时长: " + formatTime(mDurationMs));
            mVideoFrameCount = mShortVideoTrimmer.getVideoFrameCount(false);
            if (!trim && !videoPath.startsWith("rtsp:")) {
                mPreview.setVideoPath(videoPath);
            } else {
                mPreview.setVideoURI(Uri.parse(videoPath));
                mPreview.requestFocus();
                mPreview.start();
            }
            mPreview.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    play();
                }
            });
            mPreview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    controllIv.setVisibility(View.VISIBLE);
                    controllIv.animate().scaleX(3f).scaleY(3f).alpha(0.3f).setDuration(1000).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            controllIv.setVisibility(View.GONE);
                        }
                    }).start();

                }
            });
            mPreview.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (mPreview.isPlaying()) {
                        currentPosition = mPreview.getCurrentPosition();
                        controllIv.setImageResource(R.drawable.ic_play);
                        controllIv.setVisibility(View.VISIBLE);
                        controllIv.animate().scaleX(2f).scaleY(2f).start();
                        mPreview.pause();
                    } else {
                        mPreview.seekTo(currentPosition);
                        mPreview.start();
                        controllIv.setImageResource(R.drawable.ic_pause);
                        controllIv.setVisibility(View.VISIBLE);
                        controllIv.animate().scaleX(3f).scaleY(3f).alpha(0.3f).setDuration(1000).withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                controllIv.setVisibility(View.GONE);
                            }
                        }).start();
                    }
                    return false;
                }
            });
        } catch (Exception e) {
        } catch (Throwable e) {
        }
        initVideoFrameList();
    }

    private void initVideoFrameList() {
        mFrameListView = (LinearLayout) findViewById(R.id.video_frame_list);
        mHandlerLeft = findViewById(R.id.handler_left);
        mHandlerRight = findViewById(R.id.handler_right);
        mHandlerLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                float viewX = v.getX();
                float movedX = event.getX();
                float finalX = viewX + movedX;
                updateHandlerLeftPosition(finalX);

                if (action == MotionEvent.ACTION_UP) {
                    calculateRange();
                }

                return true;
            }
        });

        mHandlerRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                float viewX = v.getX();
                float movedX = event.getX();
                float finalX = viewX + movedX;
                updateHandlerRightPosition(finalX);

                if (action == MotionEvent.ACTION_UP) {
                    calculateRange();
                }

                return true;
            }
        });

        mFrameListView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mFrameListView.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                final int sliceEdge = mFrameListView.getWidth() / SLICE_COUNT;
                mSlicesTotalLength = sliceEdge * SLICE_COUNT;
                Log.i(TAG, "slice edge: " + sliceEdge);
                final float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());

                new AsyncTask<Void, PLVideoFrame, Void>() {
                    @Override
                    protected Void doInBackground(Void... v) {
                        for (int i = 0; i < SLICE_COUNT; ++i) {
                            try {
                                PLVideoFrame frame = mShortVideoTrimmer.getVideoFrameByTime((long) ((1.0f * i / SLICE_COUNT) * mDurationMs), false, sliceEdge, sliceEdge);
                                publishProgress(frame);
                            } catch (Exception e) {
                            }
                        }
                        return null;
                    }

                    @Override
                    protected void onProgressUpdate(PLVideoFrame... values) {
                        super.onProgressUpdate(values);
                        PLVideoFrame frame = values[0];
                        if (frame != null) {
                            View root = LayoutInflater.from(VideoTrimActivity.this).inflate(R.layout.frame_item, null);

                            int rotation = frame.getRotation();
                            ImageView thumbnail = (ImageView) root.findViewById(R.id.thumbnail);
                            thumbnail.setImageBitmap(frame.toBitmap());
                            thumbnail.setRotation(rotation);
                            FrameLayout.LayoutParams thumbnailLP = (FrameLayout.LayoutParams) thumbnail.getLayoutParams();
                            if (rotation == 90 || rotation == 270) {
                                thumbnailLP.leftMargin = thumbnailLP.rightMargin = (int) px;
                            } else {
                                thumbnailLP.topMargin = thumbnailLP.bottomMargin = (int) px;
                            }
                            thumbnail.setLayoutParams(thumbnailLP);

                            LinearLayout.LayoutParams rootLP = new LinearLayout.LayoutParams(sliceEdge, sliceEdge);
                            mFrameListView.addView(root, rootLP);
                        }
                    }
                }.execute();
            }
        });
    }

    private void updateHandlerLeftPosition(float movedPosition) {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mHandlerLeft.getLayoutParams();
        if ((movedPosition + mHandlerLeft.getWidth()) > mHandlerRight.getX()) {
            lp.leftMargin = (int) (mHandlerRight.getX() - mHandlerLeft.getWidth());
        } else if (movedPosition < 0) {
            lp.leftMargin = 0;
        } else {
            lp.leftMargin = (int) movedPosition;
        }
        mHandlerLeft.setLayoutParams(lp);
    }

    private void updateHandlerRightPosition(float movedPosition) {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mHandlerRight.getLayoutParams();
        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
        lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        if (movedPosition < (mHandlerLeft.getX() + mHandlerLeft.getWidth())) {
            lp.leftMargin = (int) (mHandlerLeft.getX() + mHandlerLeft.getWidth());
        } else if ((movedPosition + (mHandlerRight.getWidth() / 2)) > (mFrameListView.getX() + mSlicesTotalLength)) {
            lp.leftMargin = (int) ((mFrameListView.getX() + mSlicesTotalLength) - (mHandlerRight.getWidth() / 2));
        } else {
            lp.leftMargin = (int) movedPosition;
        }
        mHandlerRight.setLayoutParams(lp);
    }

    private float clamp(float origin) {
        if (origin < 0) {
            return 0;
        }
        if (origin > 1) {
            return 1;
        }
        return origin;
    }

    private void calculateRange() {
        float beginPercent = 1.0f * ((mHandlerLeft.getX() + mHandlerLeft.getWidth() / 2) - mFrameListView.getX()) / mSlicesTotalLength;
        float endPercent = 1.0f * ((mHandlerRight.getX() + mHandlerRight.getWidth() / 2) - mFrameListView.getX()) / mSlicesTotalLength;
        beginPercent = clamp(beginPercent);
        endPercent = clamp(endPercent);
        mSelectedBeginMs = (long) (beginPercent * mDurationMs);
        mSelectedEndMs = (long) (endPercent * mDurationMs);

        updateRangeText();
        play();
    }

    public void onDone() {
        isTrimOk = false;
        Log.i(TAG, "trim to file path: " + Config.TRIM_FILE_PATH + " range: " + mSelectedBeginMs + " - " + mSelectedEndMs + "--" + mDurationMs);
        mProcessingDialog.show();
        try {
            mShortVideoTrimmer.trim(Math.max(1, mSelectedBeginMs), Math.min(mSelectedEndMs - 1, mDurationMs), PLShortVideoTrimmer.TRIM_MODE.FAST, new PLVideoSaveListener() {
                @Override
                public void onSaveVideoSuccess(String path) {
                    isTrimOk = true;
                    renameFile(path);
                }

                @Override
                public void onSaveVideoFailed(int errorCode) {
                    mProcessingDialog.dismiss();
                }

                @Override
                public void onSaveVideoCanceled() {
                    mProcessingDialog.dismiss();
                }

                @Override
                public void onProgressUpdate(float percentage) {
                    mProcessingDialog.setProgress((int) (100 * percentage));
                }
            });
        } catch (Exception e) {
            showFailedDialog();
        } catch (Throwable throwable) {
            showFailedDialog();
        }

    }

    private void showFailedDialog() {
        AppUtils.showAlertDialog(VideoTrimActivity.this, "抱歉,我已经尽力了,但裁剪还是失败了", R.string.cancel, R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    private void renameFile(String path) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                //ToastUtils.s(VideoTrimActivity.this, "视频已保存到/sdcard/C-Video/Trim文件夹下");
                mProcessingDialog.dismiss();
                // QiaomuPlaybackActivity.start(VideoTrimActivity.this, trimed_path);
            }
        }, 1000);
        File original = new File(file_path);
        File trimmedFile = new File(path);
        String fileName = original.getName();
        String prefix = fileName.split("\\.")[0] + ".mp4";
        String rename = "trimmed_" + formatTime(mSelectedBeginMs) + "_" + formatTime(mSelectedEndMs) + "_" + prefix;
        final String renamePath = Config.FILE_PATH_TRIMMED + "/" + rename;
        AppUtils.renameFile(VideoTrimActivity.this, Config.FILE_PATH_TRIMMED, rename, path, !TextUtils.equals(file_path, path));
        trimed_path = renamePath;

        ShareDialog.newInstance(file_path).show(getSupportFragmentManager());
    }

    public void onBack(View v) {
        finish();
    }

    private String formatTime(long timeMs) {
        return String.format(Locale.CHINA, "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(timeMs),
                TimeUnit.MILLISECONDS.toSeconds(timeMs) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeMs))
        );
    }

    private void updateRangeText() {
        TextView range = (TextView) findViewById(R.id.range);
        range.setText("剪裁范围: " + formatTime(mSelectedBeginMs) + " - " + formatTime(mSelectedEndMs));
    }

    public void onShare() {
        if (TextUtils.isEmpty(trimed_path)) {
            return;
        }
        ShareDialog.newInstance(trimed_path).show(getSupportFragmentManager());
    }

    @Override
    public String getPackageName() {
        return !isTrimOk ? "com.qiniu.pili.droid.shortvideo.demo" : super.getPackageName();
    }


    public int getScreenWidth() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);

        return displayMetrics.widthPixels;
    }

    public int getScreenHeight() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }
}
