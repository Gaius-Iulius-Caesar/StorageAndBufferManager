package edu.ustc.buffer;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

/**
 * @author Wu Sai
 * @date 2023.01.08
 * @description 定义LRU双链表
 **/
@Data
public class LRU {
    private LRUEle lru;
    private LRUEle mru;

    public void removeLRUEle(int frame_id) {
        // 处理在链表两端的特殊情况
        if (this.lru != null && this.lru.getBcb().frame_id == frame_id) {
            this.lru = this.lru.getPost_LRUEle();
            this.lru.setPre_LRUEle(null);
        } else if (this.mru != null && this.mru.getBcb().frame_id == frame_id) {
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

    /**
     * @param frame_id: 帧号
     * @return 返回帧号对应的LRUEle，如果没有返回null
     */
    public LRUEle getLRUEle(int frame_id) {
        LRUEle p = this.mru;
        while (p != null && p.getBcb().frame_id != frame_id)
            p = p.getPre_LRUEle();
        if (p == null) {
            System.out.println("getLRUEle异常：未在LRU链表中找到相应的页帧");
        }
        return p;
    }

    /**
     * @param bcb: 要新建的LRU对应的BCB
     * @description 新建LRU默认在mru端
     */
    public void addLRUEle(BCB bcb) {
        LRUEle newLRUEle = new LRUEle();
        newLRUEle.setBcb(bcb);
        if (this.lru == null && this.mru == null)
            this.lru = this.mru = newLRUEle;
        this.mru.setPost_LRUEle(newLRUEle);
        newLRUEle.setPre_LRUEle(this.mru);
        newLRUEle.setPost_LRUEle(null);
        this.mru = newLRUEle;
    }

    /**
     * @param lruEle: 要移动的LRUEle
     * @description 将一个LRU元素移动到mru端
     */
    public void moveToMru(@NotNull LRUEle lruEle) {
        if(lruEle.getPost_LRUEle() == null)
            return; // 已经在mru端，无需处理
        else if (lruEle.getPre_LRUEle() == null) {
            // 在lru端，特殊处理
            this.lru = lruEle.getPost_LRUEle();
            this.lru.setPre_LRUEle(null);
            lruEle.setPost_LRUEle(null);
            this.mru.setPost_LRUEle(lruEle);
            lruEle.setPre_LRUEle(this.mru);
            this.mru = lruEle;
        }
        else {
            lruEle.getPre_LRUEle().setPost_LRUEle(lruEle.getPost_LRUEle());
            lruEle.getPost_LRUEle().setPre_LRUEle(lruEle.getPre_LRUEle());
            this.mru.setPost_LRUEle(lruEle);
            lruEle.setPre_LRUEle(this.mru);
            lruEle.setPost_LRUEle(null);
            this.mru = lruEle;
        }
    }
}
