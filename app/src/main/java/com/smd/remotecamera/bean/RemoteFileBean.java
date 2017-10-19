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

    public static ArrayList<RemoteFileBean> getList() {
        ArrayList<RemoteFileBean> list = new ArrayList<>();
        RemoteFileBean fileBean = new RemoteFileBean();
        try {
            fileBean.setUrl(new URL("http://180.97.241.48/youku/6977B2F04AF3B817A375724A46/030002080056EECA04F69A03BAF2B1BBADCA22-B1B9-E915-C03B-B0E7B0726C73.flv?sid=0508382292653301b9250_00&ctype=30"));
            fileBean.setName("https://ss2.baidu.com/6ONYsjip0QIZ8tyhnq/it/u=3625546877,4283374102&fm=173&s=651917D748A2A101EBA0FCFE0300C039&w=218&h=146&img.JPEG");
            fileBean.setDownloaded(false);
            fileBean.setDate("2017/10/19 11:07");
            fileBean.setSize(200000L);
            fileBean.setTimeStr("2017/10/19 11:07");
            list.add(fileBean);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
