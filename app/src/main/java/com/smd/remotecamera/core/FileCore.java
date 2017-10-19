package com.smd.remotecamera.core;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class FileCore {

    private boolean mCanSave = true;

    public boolean saveToFile(InputStream is, String fileStr, OnPrgListener onPrgListener) {
        File file = new File(fileStr);
        if (file.exists()) {
            file.delete();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        BufferedInputStream bis = new BufferedInputStream(is);
        RandomAccessFile raf = null;
        byte[] buff = new byte[1024 * 8];
        int prg = 0;
        int count = 0;
        try {
            raf = new RandomAccessFile(file, "rw");
            while (mCanSave && (count = bis.read(buff)) > 0) {
                raf.write(buff, 0, count);
                prg += count;
                if (onPrgListener != null) {
                    onPrgListener.onPrgChanged(prg);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (raf != null) {
                    raf.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public void stop() {
        mCanSave = false;
    }

    public interface OnPrgListener {
        void onPrgChanged(int prg);
    }

}
