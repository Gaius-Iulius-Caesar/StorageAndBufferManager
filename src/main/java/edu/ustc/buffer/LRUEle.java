package edu.ustc.buffer;


/**
 * @author Wu Sai
 * @date 2023.01.08
 * @description 定义LRU双链表元素, 注意不要使用Getter和Setter，会导致栈溢出
 **/
public class LRUEle {
    public BCB bcb;
    public LRUEle pre_LRUEle;
    public LRUEle post_LRUEle;
}
