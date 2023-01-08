package edu.ustc.buffer;

import lombok.Data;

@Data
public class LRUEle {
    private BCB bcb;
    private double time;
    private LRUEle pre_LRUEle;
    private LRUEle post_LRUEle;
}
