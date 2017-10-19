package com.smd.remotecamera.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.smd.remotecamera.R;
import com.smd.remotecamera.bean.RemoteFileBean;
import com.smd.remotecamera.constants.FileConstants;
import com.smd.remotecamera.controller.RemoteFileController;
import com.smd.remotecamera.core.DownloadCore;
import com.smd.remotecamera.util.CommonUtil;
import com.smd.remotecamera.util.Util;
import com.smd.remotecamera.view.RoundProgressBar;
import com.smd.remotecamera.view.SquareImageView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.MyHolder> implements CompoundButton.OnCheckedChangeListener,
        DownloadCore.OnDownloadProgressChangedListener {

    private Context mContext;
    private List<RemoteFileBean> mData;
    private List<RemoteFileBean> mCheckedData;
    private Map<Integer, Boolean> mCheckMap = new HashMap<>();
    private Map<Integer, Boolean> mDownloadMap = new HashMap<>();

    private RemoteFileController mRemoteFileController;
    private OnCheckedNumChangedListener mOnCheckedNumChangedListener;

    private HashSet<CheckBox> mCBSet = new HashSet<>();

    private boolean mIsSingle = false;

    public FileListAdapter(Context context, List<RemoteFileBean> data) {
        mContext = context;
        mData = data;
        mCheckedData = new ArrayList<>();
        mRemoteFileController = new RemoteFileController();

    }

    public void setData(List<RemoteFileBean> data) {
        mData = data;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_rv_filelist, parent, false);
        MyHolder myHolder = new MyHolder(view);
        mCBSet.add(myHolder.cb);
        return myHolder;
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {
        holder.setTag(position);
        RemoteFileBean fileBean = mData.get(position);
//        if (fileBean.getRoundProgressBar() != null) {
//            fileBean.getRoundProgressBar().setVisibility(View.GONE);
//        }
        fileBean.setRoundProgressBar(holder.rpb);
        holder.tvDate.setText(fileBean.getDate());
        holder.tvTime.setText(fileBean.getTime());
        holder.tvSize.setText(Util.getSizeOfM(fileBean.getSize()));
        holder.cb.setChecked(mCheckMap.get(position) == null ? false : mCheckMap.get(position));
        holder.rpb.setVisibility(mDownloadMap.get(position) == null ? View.GONE : View.VISIBLE);
//        CommonUtil.SYSO("filelistadapter", "bind: " + position + " : " + holder.rpb.getVisibility());

        String thumbFileName = fileBean.getName().split("\\.")[0] + "." + FileConstants.POSTFIX_PHOTO;
        File thumbFile = new File(FileConstants.LOCAL_THUMB_PATH + "/" + thumbFileName);
        Picasso.with(mContext).load(thumbFile)
                .placeholder(R.drawable.timg)
                .error(R.drawable.timg)
                .noFade()
                .fit()
                .into(holder.iv);
        if (!thumbFile.exists()) {
            if (fileBean.getUrl() != null) {
                mRemoteFileController.loadImage(holder.iv, fileBean.getUrl().toString(), thumbFileName);
            } else {
                String localThumb = Util.getLocalThumb(fileBean.getName());
                if (localThumb != null) {
                    Picasso.with(mContext).load(Uri.fromFile(new File(localThumb)))
                            .placeholder(R.drawable.timg)
                            .error(R.drawable.timg)
                            .noFade()
                            .fit()
                            .into(holder.iv);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return (mData == null) ? 0 : mData.size();
    }

    public void setSingle(boolean single) {
        mIsSingle = single;
    }

    @Override
    public void onDownloadProgressChanged(RemoteFileBean remoteFileBean, final long maxProgress, final long progress) {
        RemoteFileBean bean;
        for (int i = 0; i < mData.size(); i++) {
            bean = mData.get(i);
            final int index = i;
            final RoundProgressBar roundProgressBar = bean.getRoundProgressBar();
            if (bean.getName().equals(remoteFileBean.getName())) {
                if (!(mDownloadMap.get(i) == null ? false : mDownloadMap.get(i))) {
                    mDownloadMap.put(i, true);
                    CommonUtil.SYSO("filelistadapter", "" + i);
                }
                if (roundProgressBar.getMax() != maxProgress) {
                    CommonUtil.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            roundProgressBar.setVisibility(View.VISIBLE);
                        }
                    });
                    roundProgressBar.setMax(maxProgress);
                }
                roundProgressBar.setProgress(progress, i);
                if (((int) roundProgressBar.getTag()) == i) {
                    CommonUtil.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            if (maxProgress == progress) {
                                roundProgressBar.setVisibility(View.GONE);
                                mDownloadMap.remove(index);
                            } else {
//                                roundProgressBar.setVisibility(View.VISIBLE);
                                CommonUtil.SYSO("filelistadapter", "visible : " + index);
                            }
                        }
                    });
                }
            }
        }
    }

    public class MyHolder extends RecyclerView.ViewHolder {
        private SquareImageView iv;
        private CheckBox cb;
        private TextView tvDate;
        private TextView tvTime;
        private TextView tvSize;
        private RoundProgressBar rpb;

        public MyHolder(View itemView) {
            super(itemView);
            iv = (SquareImageView) itemView.findViewById(R.id.item_rv_filelist_iv);
            cb = (CheckBox) itemView.findViewById(R.id.item_rv_filelist_cb);
            tvDate = (TextView) itemView.findViewById(R.id.item_rv_filelist_tv_date);
            tvTime = (TextView) itemView.findViewById(R.id.item_rv_filelist_tv_time);
            tvSize = (TextView) itemView.findViewById(R.id.item_rv_filelist_tv_size);
            rpb = (RoundProgressBar) itemView.findViewById(R.id.item_rv_filelist_rpb_download);
            cb.setOnCheckedChangeListener(FileListAdapter.this);
        }

        public void setTag(int tag) {
            cb.setTag(tag);
            rpb.setTag(tag);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView != null) {
            if (isChecked) {
                if (mIsSingle) {
                    mCheckMap.clear();
                    mCheckedData.clear();
                    for (CheckBox checkBox : mCBSet) {
                        if (!buttonView.equals(checkBox)) {
                            checkBox.setChecked(false);
                        }
                    }
                }
                mCheckMap.put((Integer) buttonView.getTag(), true);
                mCheckedData.add(mData.get((Integer) buttonView.getTag()));
            } else {
                mCheckMap.remove((Integer) buttonView.getTag());
                mCheckedData.remove(mData.get((Integer) buttonView.getTag()));
            }
            if (mOnCheckedNumChangedListener != null) {
                mOnCheckedNumChangedListener.onCheckedNumChanged(mCheckedData, this);
            }
        }
    }

    public void setOnCheckedNumChangedListener(OnCheckedNumChangedListener onCheckedNumChangedListener) {
        mOnCheckedNumChangedListener = onCheckedNumChangedListener;
        mOnCheckedNumChangedListener.onCheckedNumChanged(mCheckedData, this);
    }

    public interface OnCheckedNumChangedListener {
        void onCheckedNumChanged(List<RemoteFileBean> checkedList, DownloadCore.OnDownloadProgressChangedListener onDownloadProgressChangedListener);
    }

    public void onShow() {
        if (mOnCheckedNumChangedListener != null) {
            mOnCheckedNumChangedListener.onCheckedNumChanged(mCheckedData, this);
        }
    }

}
