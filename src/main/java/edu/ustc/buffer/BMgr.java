package edu.ustc.buffer;


import edu.ustc.common.Constants;
import edu.ustc.dataStorage.DSMgr;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * @author Wu Sai
 * @date 2023.01.07
 * @description 定义缓冲区管理器结构
 **/
public class BMgr {
    // 哈希表
    private final int[] ftop = new int[Constants.DEFBUFSIZE];
    private final BCB[] ptof = new BCB[Constants.DEFBUFSIZE];
    // 缓冲区
    private final BFrame[] buf = new BFrame[Constants.DEFBUFSIZE];
    // 需要用到DSMgr的方法
    private final DSMgr dSMgr = new DSMgr();
    // LRU双链表
    private final LRU lRU = new LRU();
    // 命中计数器
    public static int HitCounter = 0;


    // 构造函数
    public BMgr() {
        super();
        for (int i = 0; i < Constants.DEFBUFSIZE; i++) {
            this.ptof[i] = null;// 初始化BCB数组
            this.ftop[i] = -1;
        }
        if (this.dSMgr.openFile("data.dbf") == 0)
            System.out.println("文件打开异常");
    }

    // 缓冲区关闭之前应关闭文件
    protected void finalize() {
        dSMgr.closeFile();
    }

    // 接口函数

    /**
     * @param page_id: 要调入的页号
     * @param prot: 读写标志
     * @return frame_id
     * @description 该函数查看页面是否已经在缓冲区中，如果是，则返回相应的 frame_id。如果该页还没有驻留在缓冲区中，
     * 如果需要，它会选择一个牺牲页，并加载到所请求的页中。
     */
    public int fixPage(int page_id, int prot) {
        BCB bcb = ptof[hash(page_id)];
        while (bcb != null && bcb.page_id != page_id)
            bcb = bcb.next;
        if (bcb == null) {
            // 不在buffer
            // 1. 获取替换的帧号
            int vFrameId = this.selectVictim();
            // 2. 新建BCB
            BCB newBCB = new BCB();
            newBCB.page_id = page_id;
            newBCB.frame_id = vFrameId;
            newBCB.count++;
            // 3. 如果这个帧已被使用，则首先移除这个帧
            if (ftop[vFrameId] != -1) {
                // 3.1 找到这个帧原先的BCB
                BCB vBCB = ptof[hash(ftop[vFrameId])];
                while (vBCB != null && vBCB.frame_id != vFrameId) {
                    vBCB = vBCB.next;
                }
                if (vBCB == null) {
                    System.out.println("fixPage异常: selectVictim未找到有效的页帧");
                    return -1;
                }
                // 3.2 移除这个帧对应的BCB、LRUEle，修改hash表
                this.removeBCB(vBCB, vBCB.page_id);
                this.removeLRUEle(vBCB.frame_id);
                this.ftop[vBCB.frame_id] = -1;
            }
            //4. 加载新调入页面，即修改hash表和LRU链表
            this.ftop[newBCB.frame_id] = newBCB.page_id;
            BCB temp = this.ptof[hash(newBCB.page_id)];
            if (temp == null)
                this.ptof[hash(newBCB.page_id)] = newBCB;
            else {
                while (temp.next != null)
                    temp = temp.next;
                temp.next = newBCB;
            }
            this.lRU.addLRUEle(newBCB);
            // 5. 根据读写的不同修改缓冲区
            if(prot == 0)
                // 对读操作，要读入调入的页面
                this.buf[newBCB.frame_id] = dSMgr.readPage(newBCB.page_id);
            else {
                // 对写操作，要分配一个缓冲区页帧
                this.buf[newBCB.frame_id] = new BFrame(new byte[Constants.FRAMESIZE]);
            }
            return newBCB.frame_id;
        } else {
            // 在buffer中命中，计数器增加
            HitCounter++;
            // 修改LRU链表
            LRUEle p = this.getLRUEle(bcb.frame_id);
            if (p == null)
                System.out.println("fixPage异常: 命中时LRU链表中未找到对应的页帧");
            else this.lRU.moveToMru(p);
            bcb.count++;
            return bcb.frame_id;
        }
    }

    /**
     * 由于实验只要求IO的统计，不涉及页面内容的具体读写，所以此函数实际上不会用到
     *
     * @return page_id
     * @description 分配一个未使用的新页面
     */
    public int fixNewPage() {
        // 磁盘已满
        if (dSMgr.getNumPages() == dSMgr.getPages().length)
            return -1;
        for (int page_id = 0; page_id < dSMgr.getPages().length; page_id++) {
            if (dSMgr.getPages()[page_id] == 0) {
                dSMgr.setUse(page_id, Constants.MAXPAGES);// 简单起见默认整个页面都被使用了
                dSMgr.incNumPages();
                fixPage(page_id, 0);
                return page_id;
            }
        }
        return -1;
    }

    /**
     * @param page_id: 释放的页号
     * @return frame_id
     * @description 从缓冲区释放页面，即递减页面对应的BCB计数器
     */
    public int unFixPage(int page_id) {
        BCB bcb = ptof[hash(page_id)];
        while (bcb != null && bcb.page_id != page_id)
            bcb = bcb.next;
        if (bcb == null)
            return -1;
        else {
            bcb.count--;
            return bcb.frame_id;
        }
    }

    /**
     * @return 第一个可用的frame_id
     */
    public int numFreeFrames() {
        int i = 0;
        while ((i < Constants.DEFBUFSIZE) && (ftop[i] != -1)) {
            ++i;
        }
        if (i == Constants.DEFBUFSIZE) {
            return -1;
        } else {
            return i;
        }
    }

    // 内部函数

    /**
     * @return frame_id
     * @description 使用LRU策略找到可以被替换的帧号（注意此帧可能为空）
     */
    public int selectVictim() {
        int vFrame_id = this.numFreeFrames();
        if (vFrame_id != -1)
            return vFrame_id;
        else {
            LRUEle p = this.lRU.getLru();
            while (p.bcb.count != 0)
                p = p.post_LRUEle;
            return p.bcb.frame_id;
        }
    }

    public int hash(int page_id) {
        return page_id % Constants.DEFBUFSIZE;
    }

    /**
     * @param ptr:     要移除的BCB
     * @param page_id: 要移除的页号
     * @description 在移除时，还会将脏页写回。
     * 由于使用了hash，单纯的页号并不能定位BCB，所以需要ptr和page_id两个参数
     */
    public void removeBCB(@NotNull BCB ptr, int page_id) {
        BCB bcb = ptof[hash(page_id)];
        if (bcb == null)
            return;
        if (bcb == ptr) {
            ptof[hash(page_id)] = bcb.next;
        } else {
            while (bcb.next != null && bcb.next != ptr)
                bcb = bcb.next;
            if (bcb.next == null)
                System.out.println("removeBCB异常: 未找到指定的BCB");
            bcb.next = ptr.next;
        }
        ptr.next = null;
        // 如果是脏页，需要写回
        if (ptr.dirty == 1) {
            dSMgr.writePage(page_id, buf[ptr.frame_id]);
            this.unSetDirty(ptr.frame_id);
        }
    }

    public void removeLRUEle(int frame_id) {
        this.lRU.removeLRUEle(frame_id);
    }

    public LRUEle getLRUEle(int frame_id) {
        return this.lRU.getLRUEle(frame_id);
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

    /**
     * @throws IOException 文件写异常
     * @description 写出缓冲区中可能需要写回的任何页面。如果 dirty_bit 为 1，它只会将页面写出到文件中
     */
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
        System.out.println(buf[frame_id].getField());
    }

}
