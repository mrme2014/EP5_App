package com.qiaomu.libvideo;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;


import com.qiaomu.libvideo.utils.Config;
import com.qiaomu.libvideo.utils.GetPathFromUri;
import com.qiaomu.libvideo.utils.RecordSettings;
import com.qiaomu.libvideo.utils.ToastUtils;
import com.qiaomu.libvideo.view.CustomProgressDialog;
import com.qiniu.pili.droid.shortvideo.PLShortVideoTranscoder;
import com.qiniu.pili.droid.shortvideo.PLVideoSaveListener;

import java.io.File;

public class VideoTranscodeActivity extends AppCompatActivity {
    private static final String TAG = "VideoTranscodeActivity";

    private Spinner mTranscodingBitrateLevelSpinner;
    private EditText mTranscodingWidthEditText;
    private EditText mTranscodingHeightEditText;
    private CustomProgressDialog mProcessingDialog;

    private PLShortVideoTranscoder mShortVideoTranscoder;
    private TextView mVideoFilePathText;
    private TextView mVideoSizeText;
    private TextView mVideoBitrateText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("VideoTranscode");
        setContentView(R.layout.activity_transcode);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }

        setTitle(R.string.title_transcode);

        mVideoFilePathText = (TextView) findViewById(R.id.SrcVideoPathText);
        mVideoSizeText = (TextView) findViewById(R.id.SrcVideoSizeText);
        mVideoBitrateText = (TextView) findViewById(R.id.SrcVideoBitrateText);

        mTranscodingBitrateLevelSpinner = (Spinner) findViewById(R.id.TranscodingBitrateLevelSpinner);
        mTranscodingWidthEditText = (EditText) findViewById(R.id.TranscodingWidth);
        mTranscodingHeightEditText = (EditText) findViewById(R.id.TranscodingHeight);

        ArrayAdapter<String> adapter4 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, RecordSettings.ENCODING_BITRATE_LEVEL_TIPS_ARRAY);
        mTranscodingBitrateLevelSpinner.setAdapter(adapter4);
        mTranscodingBitrateLevelSpinner.setSelection(2);

        mProcessingDialog = new CustomProgressDialog(this);
        mProcessingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mShortVideoTranscoder.cancelTranscode();
            }
        });

        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT < 19) {
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");
        } else {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("video/*");
        }
        startActivityForResult(Intent.createChooser(intent, "选择要转码的视频"), 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            String selectedFilepath = GetPathFromUri.getPath(this, data.getData());
            Log.i(TAG, "Select file: " + selectedFilepath);
            if (!TextUtils.isEmpty(selectedFilepath)) {
                onVideoFileSelected(selectedFilepath);
                return;
            }
        }

        //finish();
    }

    private void onVideoFileSelected(String filepath) {
        mShortVideoTranscoder = new PLShortVideoTranscoder(this, filepath, Config.TRANSCODE_FILE_PATH);
        mVideoFilePathText.setText(new File(filepath).getName());
        mVideoSizeText.setText(mShortVideoTranscoder.getSrcWidth() + " x " + mShortVideoTranscoder.getSrcHeight());
        mTranscodingWidthEditText.setText(String.valueOf(mShortVideoTranscoder.getSrcWidth()), TextView.BufferType.EDITABLE);
        mTranscodingHeightEditText.setText(String.valueOf(mShortVideoTranscoder.getSrcHeight()), TextView.BufferType.EDITABLE);
        String bitrate = (mShortVideoTranscoder.getSrcBitrate() / 1000) + " kbps";
        mVideoBitrateText.setText(bitrate);
    }

    public void onClickTranscode(View v) {
        if (mShortVideoTranscoder == null) {
            ToastUtils.s(this, "请先选择转码文件！");
            return;
        }

        int transcodingBitrateLevel = mTranscodingBitrateLevelSpinner.getSelectedItemPosition();
        int transcodingWidth = Integer.parseInt(mTranscodingWidthEditText.getText().toString());
        int transcodingHeight = Integer.parseInt(mTranscodingHeightEditText.getText().toString());

        mProcessingDialog.show();
        mShortVideoTranscoder.transcode(transcodingWidth, transcodingHeight, getEncodingBitrateLevel(transcodingBitrateLevel), new PLVideoSaveListener() {
            @Override
            public void onSaveVideoSuccess(String s) {
                Log.i(TAG, "save success: " + s);
                mProcessingDialog.dismiss();
                PlaybackActivity.start(VideoTranscodeActivity.this, s);
            }

            @Override
            public void onSaveVideoFailed(final int errorCode) {
                Log.i(TAG, "save failed: " + errorCode);
                mProcessingDialog.dismiss();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.s(VideoTranscodeActivity.this, "transcode failed: " + errorCode);
                    }
                });
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
    }

    private int getEncodingBitrateLevel(int position) {
        return RecordSettings.ENCODING_BITRATE_LEVEL_ARRAY[position];
    }

}
