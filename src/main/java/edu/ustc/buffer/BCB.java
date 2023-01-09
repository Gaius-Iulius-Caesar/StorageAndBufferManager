package edu.ustc.buffer;


import lombok.Data;

/**
 * @author Wu Sai
 * @date 2023.01.07
 * @description 定义缓冲控制器结构
 **/
@Data
public class BCB {
    int page_id;
    int frame_id;
    int count;
    int dirty;
    BCB next;
}
