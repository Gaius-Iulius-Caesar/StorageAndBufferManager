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
    public LRUEle lru;
    public LRUEle mru;

    public void removeLRUEle(int frame_id) {
        // 处理在链表两端的特殊情况
        if (this.lru != null && this.lru.bcb.frame_id == frame_id) {
            this.lru = this.lru.post_LRUEle;
            this.lru.pre_LRUEle = null;
        } else if (this.mru != null && this.mru.bcb.frame_id == frame_id) {
            this.mru = this.mru.pre_LRUEle;
            this.mru.post_LRUEle = null;
        }
        else {
            // 不在两端则从lru端开始查找
            LRUEle p = this.lru;
            while (p != null && p.bcb.frame_id != frame_id)
                p = p.post_LRUEle;
            if (p == null) {
                System.out.println("removeLRUEle异常：未在LRU链表中找到相应的页帧");
            } else {
                p.pre_LRUEle.post_LRUEle = p.post_LRUEle;
                p.post_LRUEle.pre_LRUEle = p.pre_LRUEle;
            }
        }
    }

    /**
     * @param frame_id: 帧号
     * @return 返回帧号对应的LRUEle，如果没有返回null
     */
    public LRUEle getLRUEle(int frame_id) {
        LRUEle p = this.mru;
        while (p != null && p.bcb.frame_id != frame_id)
            p = p.pre_LRUEle;
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
        newLRUEle.bcb =bcb;
        if (this.lru == null && this.mru == null)
            this.lru = this.mru = newLRUEle;
        else {
            this.mru.post_LRUEle = newLRUEle;
            newLRUEle.pre_LRUEle = this.mru;
            newLRUEle.post_LRUEle = null;
            this.mru = newLRUEle;
        }
    }

    /**
     * @param lruEle: 要移动的LRUEle
     * @description 将一个LRU元素移动到mru端
     */
    public void moveToMru(@NotNull LRUEle lruEle) {
        if(lruEle.post_LRUEle == null)
            return; // 已经在mru端，无需处理
        else if (lruEle.pre_LRUEle == null) {
            // 在lru端，特殊处理
            this.lru = lruEle.post_LRUEle;
            this.lru.pre_LRUEle = null;
            lruEle.post_LRUEle = null;
            this.mru.post_LRUEle = lruEle;
            lruEle.pre_LRUEle = this.mru;
            this.mru = lruEle;
        }
        else {
            lruEle.pre_LRUEle.post_LRUEle = lruEle.post_LRUEle;
            lruEle.post_LRUEle.pre_LRUEle = lruEle.pre_LRUEle;
            this.mru.post_LRUEle = lruEle;
            lruEle.pre_LRUEle = this.mru;
            lruEle.post_LRUEle = null;
            this.mru = lruEle;
        }
    }
}
