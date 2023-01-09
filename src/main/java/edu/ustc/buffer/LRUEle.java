package edu.ustc.buffer;

import lombok.Data;

@Data
public class LRUEle {
    public BCB bcb;
    public LRUEle pre_LRUEle;
    public LRUEle post_LRUEle;
}
