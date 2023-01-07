package edu.ustc.buffer;


import edu.ustc.common.Constants;

import java.util.Arrays;

/**
 * @author Wu Sai
 * @date 2023.01.07
 * @description 定义缓冲帧结构
 **/
public class BFrame {
    char[] field = new char[Constants.FRAMESIZE];

    public BFrame(byte[] buffer) {
        super();
        this.field = Arrays.toString(buffer).toCharArray();
    }
    public BFrame() {

    }
}
