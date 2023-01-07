package edu.ustc.dataStorage;

import edu.ustc.buffer.BFrame;
import edu.ustc.common.Constants;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

/**
 * @author Wu Sai
 * @date 2023.01.07
 * @description 定义数据存储管理器结构
 **/
public class DSMgr {
    private RandomAccessFile currentFile; // 使用RandomAccessFile是为了能够使用seek
    private int numPages;
    private int[] pages = new int[Constants.MAXPAGES];

    public int openFile(String fileName) throws FileNotFoundException {
        this.currentFile = new RandomAccessFile(fileName, "rw");
        return 1;
    }

    public int closeFile() {
        try {
            this.currentFile.close();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        this.currentFile = null;
        return 1;
    }

    public BFrame readPage(int page_id) throws IOException {
        currentFile.seek(page_id * Constants.FRAMESIZE);
        byte[] buffer = new byte[Constants.FRAMESIZE];
        currentFile.read(buffer, 0, Constants.FRAMESIZE);
        BFrame temp = new BFrame(buffer);
        return temp;
    }

    public int writePage(int page_id, BFrame frm) throws IOException {
        currentFile.seek((long) page_id * Constants.FRAMESIZE);
        currentFile.write(Arrays.toString(frm.field).getBytes(), 0, Constants.FRAMESIZE);
        return Constants.FRAMESIZE;
    }

    // 多余的函数，可以直接使用RandomAccessFile的seek函数
    public int seek(int offset, int pos) {
        return 0;
    }

    public RandomAccessFile getFile() {
        return this.currentFile;
    }

    public void incNumPages() {
        numPages += 1;
    }

    public int getNumPages() {
        return this.numPages;
    }

    public void setUse(int index, int use_bit) {
        pages[index] = use_bit;
    }

    public int getUse(int index) {
        return pages[index];
    }
}
