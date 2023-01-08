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

    // 构造函数
    public DSMgr() {
        super();
        currentFile = null;
        for (int i = 0; i < Constants.MAXPAGES; i++) {
            pages[i] = 0; // 初始状态下默认user_bit=0，即未被使用
        }
    }

    public int openFile(String fileName) {
        try {
            this.currentFile = new RandomAccessFile(fileName, "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
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

    /**
     * @param page_id: 页号
     * @return 读取的页面内容
     * @throws IOException 文件读异常
     */
    public BFrame readPage(int page_id) throws IOException {
        currentFile.seek((long) page_id * Constants.FRAMESIZE);
        byte[] buffer = new byte[Constants.FRAMESIZE];
        currentFile.read(buffer, 0, Constants.FRAMESIZE);
        return new BFrame(buffer);
    }

    /**
     * @param page_id: 页号
     * @param frm: 待写入的帧内容
     * @return 写入字节数
     * @throws IOException 文件写异常
     */
    public int writePage(int page_id, BFrame frm) throws IOException {
        this.currentFile.seek((long) page_id * Constants.FRAMESIZE);
        this.currentFile.write(Arrays.toString(frm.field).getBytes(), 0, Constants.FRAMESIZE);
        return Constants.FRAMESIZE;
    }

    // 多余的函数，可以直接使用RandomAccessFile的seek函数
    public int seek(int offset, int pos) {
        return 0;
    }

    public RandomAccessFile getFile() {
        return this.currentFile;
    }

    /**
     * @description 递增页面计数器
     */
    public void incNumPages() {
        numPages += 1;
    }

    /**
     * @return 页面计数器
     */
    public int getNumPages() {
        return this.numPages;
    }

    /**
     * @param page_id: 页号
     * @param use_bit: 页面被使用的bit数
     * @description 设置 pages 数组中的元素。这个数组跟踪正在使用的页面。如果一个页
     * 面中的所有记录都被删除了，那么这个页面实际上就不再被使用了，可以在数据库中重新使用。
     * 为了知道页面是否可重用，检查数组中是否有任何被设置为零的use_bits。
     * fixNewPage 函数首先检查这个数组的 use_bit 是否为零。如果找到一个，就重用这个页面。如果没有，则分配新的页面。
     */
    public void setUse(int page_id, int use_bit) {
        pages[page_id] = use_bit;
    }

    /**
     * @param page_id: 页号
     * @return 对应 page_id 的当前 use_bit。
     */
    public int getUse(int page_id) {
        return pages[page_id];
    }
}
