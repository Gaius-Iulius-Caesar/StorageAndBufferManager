package edu.ustc.buffer;


import edu.ustc.common.Constants;
import lombok.Data;

import java.util.Arrays;

/**
 * @author Wu Sai
 * @date 2023.01.07
 * @description 定义缓冲帧结构
 **/
@Data
public class BFrame {
    private final char[] field = new char[Constants.FRAMESIZE];// 长度为Constants.FRAMESIZE

    public BFrame(byte[] buffer) {
        super();
        System.arraycopy(Arrays.toString(buffer).toCharArray(), 0, this.field, 0, this.field.length);
    }

}
