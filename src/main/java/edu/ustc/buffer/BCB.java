package edu.ustc.buffer;


/**
 * @author Wu Sai
 * @date 2023.01.07
 * @description 定义缓冲控制器结构
 **/
public class BCB {
    int page_id;
    int frame_id;
    int count;
    int dirty;
    BCB next;
}
