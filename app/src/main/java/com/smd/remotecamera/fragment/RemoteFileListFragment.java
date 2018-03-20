package com.smd.remotecamera.fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import jczj.android.com.sharelib.RoundProgressDialog;
import com.smd.remotecamera.R;
import com.smd.remotecamera.adapter.FileListAdapter;
import com.smd.remotecamera.adapter.RemoteFileListPagerAdapter;
import com.smd.remotecamera.bean.RemoteFileBean;

import java.util.ArrayList;
import java.util.List;

public class RemoteFileListFragment extends Fragment implements View.OnClickListener {

    private ImageView mIbBack;
    private TextView mTvVideo;
    private TextView mTvPhoto;
    private ViewPager mVp;

    private ListFragment mVideoListFragment;
    private ListFragment mPhotoListFragment;

    private OnClickBackListener mOnClickBackListener;
    private FileListAdapter.OnCheckedNumChangedListener mOnCheckedNumChangedListener;
    private ViewPager.OnPageChangeListener mExtOnPageChangeListener;

    private boolean mIsSingle;
    private String mNameType;
    private ViewPager.OnPageChangeListener mOnPageChangeListener1;
    ProgressDialog mProgressDialog;

    public RemoteFileListFragment() {
    }

    @SuppressLint("ValidFragment")
    public RemoteFileListFragment(boolean single, @FileListAdapter.FileNameType String nameType) {
        mIsSingle = single;
        mNameType = nameType;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_remotefile_list, container, false);
        initView(view);
        init();
      //  mProgressDialog = new RoundProgressDialog(getContext());
      //  mProgressDialog.show();
        return view;
    }

    private void initView(View view) {
        mIbBack = (ImageView) view.findViewById(R.id.fragment_remotefile_ib_back);
        mTvVideo = (TextView) view.findViewById(R.id.fragment_remotefile_tv_video);
        mTvPhoto = (TextView) view.findViewById(R.id.fragment_remotefile_tv_photo);
        mVp = (ViewPager) view.findViewById(R.id.fragment_remotefile_vp);
        mIbBack.setOnClickListener(this);
        mTvVideo.setOnClickListener(this);
        mTvPhoto.setOnClickListener(this);
        mVp.addOnPageChangeListener(mOnPageChangeListener);
        if (mExtOnPageChangeListener != null) {
            mVp.addOnPageChangeListener(mExtOnPageChangeListener);
        }
    }

    private void init() {
        List<Fragment> data = new ArrayList<>();

        mVideoListFragment = new ListFragment(mNameType);
        Bundle bundle = new Bundle();
        bundle.putBoolean("isSingle", mIsSingle);
        mVideoListFragment.setArguments(bundle);

        mPhotoListFragment = new ListFragment(mNameType);
        Bundle bundle1 = new Bundle();
        bundle1.putBoolean("isSingle", mIsSingle);
        mPhotoListFragment.setArguments(bundle1);

        mVideoListFragment.setOnCheckedNumChangedListener(mOnCheckedNumChangedListener);
        mPhotoListFragment.setOnCheckedNumChangedListener(mOnCheckedNumChangedListener);
        //mOnCheckedNumChangedListener = null;
        data.add(mVideoListFragment);
        data.add(mPhotoListFragment);
        RemoteFileListPagerAdapter adapter = new RemoteFileListPagerAdapter(getChildFragmentManager(), data);
        mVp.setAdapter(adapter);
    }

    public void setData(List<RemoteFileBean> videoData, List<RemoteFileBean> photoData) {
        mProgressDialog.dismiss();
        mVideoListFragment.setData(videoData);
        mPhotoListFragment.setData(photoData);
    }

    private void changeTopMode(int index) {
        if (index == 0) {
            mTvVideo.setCompoundDrawablesWithIntrinsicBounds(null, null, null, getResources().getDrawable(R.drawable.bottom_blue));
            mTvPhoto.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            mTvVideo.setClickable(false);
            mTvPhoto.setClickable(true);
            mVideoListFragment.notifyShow();
        } else {
            mTvVideo.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            mTvPhoto.setCompoundDrawablesWithIntrinsicBounds(null, null, null, getResources().getDrawable(R.drawable.bottom_blue));
            mTvVideo.setClickable(true);
            mTvPhoto.setClickable(false);
            mPhotoListFragment.notifyShow();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_remotefile_ib_back:
                if (mOnClickBackListener != null) {
                    mOnClickBackListener.onClickBack();
                }
                break;
            case R.id.fragment_remotefile_tv_video:
                mVp.setCurrentItem(0);
                break;
            case R.id.fragment_remotefile_tv_photo:
                mVp.setCurrentItem(1);
                break;
        }
    }

    private ViewPager.SimpleOnPageChangeListener mOnPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            changeTopMode(position);
            if (mOnPageChangeListener1 != null)
                mOnPageChangeListener1.onPageSelected(position);
        }
    };

    public void setOnClickBackListener(OnClickBackListener onClickBackListener) {
        mOnClickBackListener = onClickBackListener;
    }

    public void setonPagerSelectListener(ViewPager.OnPageChangeListener onPageChangeListener) {

        mOnPageChangeListener1 = onPageChangeListener;
    }

    public boolean ondeleted(RemoteFileBean remoteFileBean) {
        if (mVp.getCurrentItem() == 0)
            return mVideoListFragment.onDeleted(remoteFileBean);
        return mPhotoListFragment.onDeleted(remoteFileBean);
    }

    public void showDialog() {
       // mProgressDialog.show();
    }

    public void dismissDialog() {
       // mProgressDialog.dismiss();
    }

    public interface OnClickBackListener {
        void onClickBack();
    }

    public void setOnCheckedNumChangedListener(FileListAdapter.OnCheckedNumChangedListener onCheckedNumChangedListener) {
        mOnCheckedNumChangedListener = onCheckedNumChangedListener;
    }

    public void addOnPageChangeListener(ViewPager.OnPageChangeListener onPageChangeListener) {
        mExtOnPageChangeListener = onPageChangeListener;
    }

}
