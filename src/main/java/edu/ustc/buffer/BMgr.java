package edu.ustc.buffer;

import com.sun.xml.internal.ws.api.pipe.PipelineAssembler;
import edu.ustc.Main;
import edu.ustc.common.Constants;

/**
 * @author Wu Sai
 * @date 2023.01.07
 * @description 定义缓冲区管理器结构
 **/
public class BMgr {
    // 哈希表
    private int[] ftop = new int[Constants.DEFBUFSIZE];
    private BCB[] ptof = new BCB[Constants.DEFBUFSIZE];

    // 接口函数
    public int fixPage(int page_id, int prot) {
        return 0;
    }

    public void fixNewPage() {

    }

    public int unFixPage(int page_id) {
        return 0;
    }

    public int numFreeFrames() {
        return 0;
    }

    // 内部函数
    public int selectVictim() {
        return 0;
    }

    public int hash(int page_id) {
        return 0;
    }

    public void removeBCB(BCB ptr, int page_id) {

    }

    public void removeLRUEle(int frid) {

    }

    public void setDirty(int frame_id) {

    }

    public void unSetDirty(int frame_id) {

    }

    public void writeDirtys() {
    }

    public void printFrame(int frame_id) {

    }

}
