package edu.ustc.dataStorage;

import edu.ustc.buffer.BFrame;
import edu.ustc.common.Constants;

import java.io.RandomAccessFile;

/**
 * @author Wu Sai
 * @date 2023.01.07
 * @description 定义数据存储管理器结构
 **/
public class DSMgr {
    private RandomAccessFile currentFile;
    private int numPages;
    private int[] pages = new int[Constants.MAXPAGES];

    public int openFile(String fileName) {
        return 0;
    }

    public int closeFile() {
        return 0;
    }

    public BFrame readPage(int page_id) {
        return new BFrame();
    }

    public int writePage(int frame_id, BFrame frm) {
        return 0;
    }

    public int seek(int offset, int pos) {
        return 0;
    }

    public RandomAccessFile getFile() {
        return this.currentFile;
    }

    public void incNumPages() {
    }

    public int getNumPages() {
        return 0;
    }

    public void setUse(int index, int use_bit) {

    }

    public int getUse(int index) {
        return 0;
    }
}
