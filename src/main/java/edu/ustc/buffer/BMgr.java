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
    private final int[] ftop = new int[Constants.DEFBUFSIZE];
    private final BCB[] ptof = new BCB[Constants.DEFBUFSIZE];
    private final BFrame[] buf = new BFrame[Constants.DEFBUFSIZE];
    private final DSMgr dSMgr = new DSMgr(); // 需要用到DSMgr的方法
    // LRU双链表的两个表头
    private LRUEle lru;
    private LRUEle mru;

    // 构造函数
    public BMgr() throws FileNotFoundException {
        super();
        for (int i = 0; i < Constants.DEFBUFSIZE; i++) {
            this.ptof[i] = null;// 初始化BCB数组
            this.ftop[i] = -1;
        }
        this.dSMgr.openFile("../data.dbf");
    }

    // 接口函数

    /**
     * @param page_id: 要调入的页号
     * @param prot
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
            // 获取替换的BCB
            BCB vBCB = ptof[hash(ftop[this.selectVictim()])];
            while (vBCB != null && vBCB.frame_id != this.selectVictim()) {
                vBCB = vBCB.next;
            }
            if (vBCB == null) {
                System.out.println("fixPage异常: selectVictim未找到对应的页帧");
                return -1;
            }
            // 如果vBCB是脏页，则写回
            if (vBCB.dirty == 1) {
                try {
                    dSMgr.writePage(vBCB.page_id, buf[vBCB.frame_id]);
                } catch (IOException e) {
                    System.out.println("fixPage异常:脏页写回文件异常");
                }
                this.unSetDirty(vBCB.frame_id);
            }
            // 移除LRUEle并修改两个hash表
            this.removeBCB(vBCB, vBCB.page_id);
            this.ftop[vBCB.frame_id] = -1;
            this.removeLRUEle(vBCB.frame_id);
            // 为调入的页面新建BCB并修改两个hash表
            BCB newBCB = new BCB();
            newBCB.page_id = page_id;
            newBCB.frame_id = vBCB.frame_id;
            newBCB.count++;
            this.ftop[newBCB.frame_id] = newBCB.page_id;
            BCB temp = this.ptof[hash(newBCB.page_id)];
            if (temp == null)
                this.ptof[hash(newBCB.page_id)] = newBCB;
            else {
                while (temp.next != null)
                    temp = temp.next;
                temp.next = newBCB;
            }
            // 为调入的页面新建LRUELe并修改链表
            LRUEle newLRUEle = new LRUEle();
            newLRUEle.setBcb(newBCB);
            if(this.lru == null && this.mru == null)
                this.lru = this.mru = newLRUEle;
            this.mru.setPost_LRUEle(newLRUEle);
            newLRUEle.setPre_LRUEle(this.mru);
            newLRUEle.setPost_LRUEle(null);
            this.mru = newLRUEle;

            return newBCB.frame_id;
        } else {
            // 在buffer中
            // 修改LRU链表
            LRUEle p = this.getLRUEle(bcb.frame_id);
            if (p == null)
                System.out.println("fixPage异常: 命中时LRU链表中未找到对应的页帧");
            else if (p.getPost_LRUEle() != null) {
                // 只有p不是mru时才需调整
                p.getPre_LRUEle().setPost_LRUEle(p.getPost_LRUEle());
                p.getPost_LRUEle().setPre_LRUEle(p.getPre_LRUEle());
                this.mru.setPost_LRUEle(p);
                p.setPre_LRUEle(this.mru);
                p.setPost_LRUEle(null);
                this.mru = p;
            }
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
     * 由于实验只要求IO的统计，不涉及页面内容的具体读写，所以此函数实际上不会用到
     *
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
            ftop[bcb.frame_id] = -1;
            return bcb.frame_id;
        }
    }

    public int numFreeFrames() {
        int i = 0;
        while ((ftop[i] != -1) && (i < Constants.DEFBUFSIZE)) {
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
     * @description 使用LRU策略找到可以被替换的帧
     */
    public int selectVictim() {
        return this.lru.getBcb().frame_id;
    }

    public int hash(int page_id) {
        return page_id % Constants.DEFBUFSIZE;
    }

    public void removeBCB(BCB ptr, int page_id) {
        BCB bcb = ptof[hash(page_id)];
        if (bcb == null)
            return;
        if (bcb == ptr)
            ptof[hash(page_id)] = bcb.next;
        else {
            while (bcb.next != ptr) {
                if (bcb.next == null)
                    return;
                bcb = bcb.next;
            }
            bcb.next = ptr.next;
        }
    }

    public void removeLRUEle(int frame_id) {
        // 处理在链表两端的特殊情况
        if(this.lru != null && this.lru.getBcb().frame_id == frame_id) {
            this.lru = this.lru.getPost_LRUEle();
            this.lru.setPre_LRUEle(null);
        }
        else if(this.mru != null && this.mru.getBcb().frame_id == frame_id) {
            this.mru = this.mru.getPre_LRUEle();
            this.lru.setPost_LRUEle(null);
        }
        // 不在两端则从lru端开始查找
        LRUEle p = this.lru;
        while (p != null && p.getBcb().frame_id != frame_id)
            p = p.getPost_LRUEle();
        if (p == null) {
            System.out.println("removeLRUEle异常：未在LRU链表中找到相应的页帧");
        } else {
            p.getPre_LRUEle().setPost_LRUEle(p.getPost_LRUEle());
            p.getPost_LRUEle().setPre_LRUEle(p.getPre_LRUEle());
        }
    }

    public LRUEle getLRUEle(int frame_id) {
        LRUEle p = this.mru;
        while (p != null && p.getBcb().frame_id != frame_id)
            p = p.getPre_LRUEle();
        if (p == null) {
            System.out.println("getLRUEle异常：未在LRU链表中找到相应的页帧");
        }
        return p;
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
     * @description 写出缓冲区中可能需要写入的任何页面。如果 dirty_bit 为 1，它只会将页面写出到文件中
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
        System.out.println(buf[frame_id].field);
    }

}
