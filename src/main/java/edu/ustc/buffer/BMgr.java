package edu.ustc.buffer;


import edu.ustc.common.Constants;
import edu.ustc.dataStorage.DSMgr;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author Wu Sai
 * @date 2023.01.07
 * @description 定义缓冲区管理器结构
 **/
public class BMgr {
    // 哈希表
    private int[] ftop = new int[Constants.DEFBUFSIZE];
    private BCB[] ptof = new BCB[Constants.DEFBUFSIZE];
    public BFrame[] buf = new BFrame[Constants.DEFBUFSIZE];
    DSMgr dSMgr = new DSMgr(); // 需要用到DSMgr的方法

    // 构造函数
    public BMgr() throws FileNotFoundException {
        for (int i = 0; i < Constants.DEFBUFSIZE; i++) {
            this.ptof[i] = null;// 初始化BCB数组
            this.ftop[i] = -1;
        }
        this.dSMgr.openFile("../data.dbf");
    }

    // 接口函数
    public int fixPage(int page_id, int prot) {
        return 0;
    }

    public void fixNewPage() {

    }

    public int unFixPage(int page_id) {
        return 0;
    }

    public int numFreeFrames() {
        int i = 0;
        while ((ftop[i] != -1) && (i<Constants.DEFBUFSIZE)) {
            ++i;
        }
        if (i == Constants.DEFBUFSIZE) {
            return -1;
        }
        else{
            return i;
        }
    }

    // 内部函数
    public int selectVictim() {
        return 0;
    }

    public int hash(int page_id) {
        return page_id % Constants.DEFBUFSIZE;
    }

    public void removeBCB(BCB ptr, int page_id) {
        BCB bcb = ptof[hash(page_id)];
        if(bcb == null)
            return;
        if(bcb == ptr)
            ptof[hash(page_id)] = bcb.next;
        else {
            while (bcb.next != ptr){
                if(bcb.next == null)
                    return;
                bcb = bcb.next;
            }
            bcb.next = ptr.next;
        }
    }

    public void removeLRUEle(int frid) {

    }

    public void setDirty(int frame_id) {
        int pid = ftop[frame_id];
        int fid = hash(pid);
        BCB bcb = ptof[fid];
        while (bcb != null && bcb.page_id != pid) {
            bcb = bcb.next;
        }
        if (bcb != null) {
            bcb.dirty = 1;
        }
    }

    public void unSetDirty(int frame_id) {
        int pid = ftop[frame_id];
        int fid = hash(pid);
        BCB bcb = ptof[fid];
        while (bcb != null && bcb.page_id != pid) {
            bcb = bcb.next;
        }
        if (bcb != null) {
            bcb.dirty = 0;
        }
    }

    public void writeDirtys() throws IOException {
        for (BCB bcb : ptof) {
            while (bcb != null) {
                if (bcb.dirty == 1) {
                    dSMgr.writePage(bcb.page_id, buf[bcb.frame_id]);
                    this.unSetDirty(bcb.frame_id);
                }
                bcb = bcb.next;
            }
        }
    }

    public void printFrame(int frame_id) {
        System.out.println(buf[frame_id].field);
    }

}
