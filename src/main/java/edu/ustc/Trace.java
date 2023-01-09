package edu.ustc;

import edu.ustc.buffer.BMgr;
import edu.ustc.common.Constants;
import edu.ustc.dataStorage.DSMgr;
import lombok.Data;

import java.io.*;
import java.util.ArrayList;

@Data
public class Trace {
    private final BMgr bMgr = new BMgr();
    private double HitRate = 0;
    private int IOCounter = 0;

    /**
     * @description 具体化data.dbf
     */
    public void createFile() throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile("data.dbf", "rw");
        byte[] bu = new byte[Constants.FRAMESIZE];
        for (int i = 0; i < Constants.FRAMESIZE; i++) {
            bu[i] = '1';
        }
        for (int j = 0; j < Constants.MAXPAGES; j++) {
            randomAccessFile.write(bu);
        }
        randomAccessFile.close();
    }

    /**
     * 本实验不考察具体读写
     * @param page_id: 页号
     * @description 调用缓冲区进行读操作
     * bMgr.printFrame(bMgr.fixPage(page_id, 0));
     */
    public void read(int page_id) {
        bMgr.fixPage(page_id, 0);
        bMgr.unFixPage(page_id);
    }

    /**
     * 本实验不考察具体读写
     * @param page_id: 页号
     * @description 调用缓冲区进行写操作
     * 此函数省略了向缓冲区写操作的代码
     */
    public void write(int page_id) {
        bMgr.setDirty(bMgr.fixPage(page_id, 1));
        // 此处应有写入缓冲区代码，由于本实验不考察具体读写，故省略
        bMgr.unFixPage(page_id);
    }

    /**
     * @throws IOException 文件读写异常
     * @description 读取追踪文件并执行，统计IO次数、命中率
     */
    public void getStatistics() throws IOException {
        // 读文件
        BufferedReader bufferReader = new BufferedReader(new FileReader("data-5w-50w-zipf.txt"));
        String temp_str;
        ArrayList<String> arrayList = new ArrayList<>();
        while ((temp_str = bufferReader.readLine()) != null) {
            temp_str = temp_str.trim();
            if (temp_str.length() > 0)
                arrayList.add(temp_str);
        }
        bufferReader.close();

        //array拆分、识别并执行读写操作
        for (String line : arrayList) {
            String[] temp_str2 = line.split(",");
            int operation = Integer.parseInt(temp_str2[0]);
            int page_id = Integer.parseInt(temp_str2[1]) - 1; // 文件中的页号从1开始
            if (operation == 0) {      // 读操作
                this.read(page_id);
            } else {               //写操作
                this.write(page_id);
            }
        }
        // 执行结束
        this.finish();
        // 统计数据
        this.HitRate = (double) BMgr.HitCounter / arrayList.size();
        this.IOCounter = DSMgr.ICounter + DSMgr.OCounter;
    }

    /**
     * @description 结束追踪程序，写回所有脏页，关闭文件
     */
    public void finish() {
        try {
            bMgr.writeDirtys();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("追踪程序关闭异常");
        }
    }

}
