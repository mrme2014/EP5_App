package com.smd.remotecamera.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smd.remotecamera.R;
import com.smd.remotecamera.adapter.FileListAdapter;
import com.smd.remotecamera.bean.RemoteFileBean;
import com.smd.remotecamera.util.CommonUtil;

import java.util.List;


public class ListFragment extends Fragment {

    private RecyclerView mRv;

    private List<RemoteFileBean> mData;
    private FileListAdapter mAdapter;
    private boolean mIsSingle;

    private FileListAdapter.OnCheckedNumChangedListener mOnCheckedNumChangedListener;

    public ListFragment(boolean single) {
        mIsSingle = single;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.item_vp_fragmentlist, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mRv = (RecyclerView) view.findViewById(R.id.fragment_list_rv);
        mRv.setLayoutManager(new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false));
        mAdapter = new FileListAdapter(getContext(), mData);
        mAdapter.setOnCheckedNumChangedListener(mOnCheckedNumChangedListener);
        mAdapter.setSingle(mIsSingle);
        mOnCheckedNumChangedListener = null;
        mRv.setAdapter(mAdapter);
        if (mData != null) {
            mAdapter.setData(mData);
            mAdapter.notifyDataSetChanged();
        }
    }

    public void setData(List<RemoteFileBean> data) {
        mData = data;
        if (mAdapter != null) {
            mAdapter.setData(mData);
            CommonUtil.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    public void notifyShow() {
        mAdapter.onShow();
    }

    public void setOnCheckedNumChangedListener(FileListAdapter.OnCheckedNumChangedListener onCheckedNumChangedListener) {
        mOnCheckedNumChangedListener = onCheckedNumChangedListener;
    }

}
