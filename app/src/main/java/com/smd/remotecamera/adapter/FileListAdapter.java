package com.smd.remotecamera.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.qiaomu.libvideo.utils.AppUtils;
import com.qiaomu.libvideo.utils.TimeUtils;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.MyHolder> implements CompoundButton.OnCheckedChangeListener,
        DownloadCore.OnDownloadProgressChangedListener {
    public @interface FileNameType {
        String ORIGINAL = "";
        String DOWNLOAD = "A";
        String EDIT = "B";
    }

    @FileNameType
    private String fileNameType = FileNameType.ORIGINAL;

    private Context mContext;
    private List<RemoteFileBean> mData;
    private List<RemoteFileBean> mCheckedData;
    private Map<Integer, Boolean> mCheckMap = new HashMap<>();
    private Map<Integer, Boolean> mDownloadMap = new HashMap<>();

    private RemoteFileController mRemoteFileController;
    private OnCheckedNumChangedListener mOnCheckedNumChangedListener;

    private HashSet<CheckBox> mCBSet = new HashSet<>();

    private boolean mIsSingle = false;

    public FileListAdapter(Context context, List<RemoteFileBean> data, @FileNameType String nameType) {
        mContext = context;
        mData = data;
        mCheckedData = new ArrayList<>();
        mRemoteFileController = new RemoteFileController();
        this.fileNameType = nameType;
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
    public void onBindViewHolder(final MyHolder holder, int position) {
        holder.setTag(position);
        final RemoteFileBean fileBean = mData.get(position);
        fileBean.setRoundProgressBar(holder.rpb);

        String checkFileNameType = fileNameType;
        if (TextUtils.equals(fileNameType, FileNameType.DOWNLOAD) && fileBean.isHasEdit())
            checkFileNameType = FileNameType.EDIT;
        String format = "";
        if (position < 10) {
            format = "-" + checkFileNameType + ("0000" + (position + 1));
        } else if (position >= 10 && position < 100) {
            format = "-" + checkFileNameType + ("00" + (position + 1));
        } else if (position >= 100) {
            format = "-" + checkFileNameType + ("0" + (position + 1));
        }

        holder.tvTime.setText(TimeUtils.format(fileBean.getDate()) + format);
        holder.tvSize.setText(Util.getSizeOfM(fileBean.getSize()));
        holder.cb.setChecked(mCheckMap.get(position) == null ? false : mCheckMap.get(position));
        holder.rpb.setVisibility(mDownloadMap.get(position) == null ? View.GONE : View.VISIBLE);

        String thumbFileName = fileBean.getName().split("\\.")[0] + "." + FileConstants.POSTFIX_PHOTO;
        File thumbFile = new File(FileConstants.LOCAL_THUMB_PATH + "/" + thumbFileName);

        if (fileBean.getUrl() != null) {
            mRemoteFileController.loadImage(holder.iv, fileBean.getUrl().toString(), thumbFileName);
        } else {
            if (thumbFile.exists()) {
                Picasso.with(mContext).load(Uri.fromFile(thumbFile))
                        .placeholder(R.drawable.timg)
                        .error(R.drawable.timg)
                        .into(holder.iv);
            } else {
                holder.iv.setImageResource(R.drawable.timg);
            }
        }

        holder.iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!fileBean.getName().endsWith(FileConstants.POSTFIX_PHOTO)) {
                    String file_path = FileConstants.LOCAL_VIDEO_PATH + File.separator + fileBean.getName();
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(AppUtils.getUri(mContext, file_path), "video/*");
                    mContext.startActivity(intent);
                }
            }
        });


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
            if (TextUtils.equals(bean.getName(), remoteFileBean.getName())) {
                if (!(mDownloadMap.get(i) == null ? false : mDownloadMap.get(i))) {
                    mDownloadMap.put(i, true);
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
//
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
            try {
                cb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (cb != null) {
                            if (cb.isChecked()) {
                                if (mIsSingle) {
                                    mCheckMap.clear();
                                    mCheckedData.clear();
                                    for (CheckBox checkBox : mCBSet) {
                                        if (!cb.equals(checkBox)) {
                                            checkBox.setChecked(false);
                                        }
                                    }
                                }
                                mCheckMap.put((Integer) cb.getTag(), true);
                                // mCheckedData.add(mData.get((Integer) cb.getTag()));
                            } else {
                                mCheckMap.remove((Integer) cb.getTag());
                                //  boolean remove = mCheckedData.remove(mData.get((Integer) cb.getTag()));
                            }
                            if (mOnCheckedNumChangedListener != null) {
                                mCheckedData.clear();
                                Set<Integer> integers = mCheckMap.keySet();
                                Iterator<Integer> iterator = integers.iterator();
                                while (iterator.hasNext()) {
                                    Integer next = iterator.next();
                                    mCheckedData.add(mData.get(next.intValue()));
                                }
                                mOnCheckedNumChangedListener.onCheckedNumChanged(mCheckedData, FileListAdapter.this);
                            }
                        }
                    }
                });
            } catch (Exception e) {
            }
        }

        public void setTag(int tag) {
            cb.setTag(tag);
            rpb.setTag(tag);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    }

    public void setOnCheckedNumChangedListener(OnCheckedNumChangedListener onCheckedNumChangedListener) {
        mOnCheckedNumChangedListener = onCheckedNumChangedListener;
        if (mOnCheckedNumChangedListener != null)
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
