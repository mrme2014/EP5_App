package com.smd.remotecamera.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ntk.nvtkit.S;
import com.smd.remotecamera.R;
import com.smd.remotecamera.adapter.FileListAdapter;
import com.smd.remotecamera.bean.RemoteFileBean;
import com.smd.remotecamera.util.CommonUtil;

import java.util.List;

import jczj.android.com.sharelib.RoundProgressDialog;


public class ListFragment extends Fragment {

    private RecyclerView mRv;

    private List<RemoteFileBean> mData;
    private FileListAdapter mAdapter;
    private boolean mIsSingle;

    private FileListAdapter.OnCheckedNumChangedListener mOnCheckedNumChangedListener;
    private View view;
    private String mNameType;



    public ListFragment() {
    }

    @SuppressLint("ValidFragment")
    public ListFragment(@FileListAdapter.FileNameType String nameType) {
        mNameType = nameType;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.item_vp_fragmentlist, container, false);
        initView(view);
        mIsSingle = getArguments().getBoolean("isSingle");
        return view;
    }

    private void initView(View view) {



        mRv = (RecyclerView) view.findViewById(R.id.fragment_list_rv);
        mRv.setLayoutManager(new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false));
        RecyclerView.RecycledViewPool recycledViewPool = mRv.getRecycledViewPool();
        recycledViewPool.setMaxRecycledViews(0, 10);
        mAdapter = new FileListAdapter(getContext(), mData, mNameType);
        mAdapter.setOnCheckedNumChangedListener(mOnCheckedNumChangedListener);
        mAdapter.setSingle(mIsSingle);
        //mOnCheckedNumChangedListener = null;
        RecyclerView.RecycledViewPool pool = mRv.getRecycledViewPool();
        pool.setMaxRecycledViews(0, 20);
        mRv.setRecycledViewPool(pool);
        mRv.setAdapter(mAdapter);

        if (mAdapter == null || mAdapter.getItemCount() <= 0) {
            ((TextView) view.findViewById(R.id.emptyTipTv)).setText("空空如也...");
            view.findViewById(R.id.empty_layout).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.empty_layout).setVisibility(View.GONE);
        }

        if (mData != null) {
            mAdapter.setData(mData);
            mAdapter.notifyDataSetChanged();
        }

    }

    public void setData(final List<RemoteFileBean> data) {
        mData = data;
        if (mAdapter != null) {
            mAdapter.setData(mData);
            CommonUtil.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyDataSetChanged();
                    if (view != null && mAdapter.getItemCount() > 0)
                        view.findViewById(R.id.empty_layout).setVisibility(View.GONE);
                }
            });
        }

    }

    public void notifyShow() {
        if (mAdapter != null) mAdapter.onShow();
    }

    public void setOnCheckedNumChangedListener(FileListAdapter.OnCheckedNumChangedListener onCheckedNumChangedListener) {
        mOnCheckedNumChangedListener = onCheckedNumChangedListener;
    }

    public boolean onDeleted(RemoteFileBean remoteFileBean) {
        int index = -1;
        for (int i = 0; i < mData.size(); i++) {
            if (TextUtils.equals(mData.get(i).getName(), remoteFileBean.getName())) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            mAdapter.onDelete(index);
        }


        if (mAdapter.getItemCount() == 0) {
            view.findViewById(R.id.empty_layout).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.emptyTipTv)).setText("空空如也~");
        }


        return mAdapter.getItemCount() == 0;
    }
}
