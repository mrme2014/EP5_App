package com.smd.remotecamera.bean;


import com.smd.remotecamera.view.RoundProgressBar;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class RemoteFileBean {

    private String name;
    private URL url;
    private long size;
    private String timeStr;
    private String date;
    private String time;
    private boolean isDownloaded;

    private RoundProgressBar roundProgressBar;
    private boolean mExists;

    public RemoteFileBean() {
    }

    public RemoteFileBean(String name, URL url, long size, String timeStr) {
        this(name, url, size, timeStr, false);
    }

    public RemoteFileBean(String name, URL url, long size, String timeStr, boolean isDownloaded) {
        this.name = name;
        this.url = url;
        this.size = size;
        setTimeStr(timeStr);
        this.isDownloaded = isDownloaded;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getTimeStr() {
        return timeStr;
    }

    public void setTimeStr(String timeStr) {
        this.timeStr = timeStr;
        date = this.timeStr.split(" ")[0];
        date = date.replace('/', '-');
        time = this.timeStr.split(" ")[1];
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public void setDownloaded(boolean downloaded) {
        isDownloaded = downloaded;
    }

    public RoundProgressBar getRoundProgressBar() {
        return roundProgressBar;
    }

    public void setRoundProgressBar(RoundProgressBar roundProgressBar) {
        this.roundProgressBar = roundProgressBar;
    }

    public void setHasEdit(boolean exists) {
        mExists = exists;
    }

    public boolean isHasEdit() {
        return mExists;
    }
}
