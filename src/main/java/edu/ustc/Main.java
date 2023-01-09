package edu.ustc;

import edu.ustc.buffer.BMgr;

import javax.imageio.IIOException;
import javax.management.openmbean.TabularType;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Trace trace = new Trace();
        // 具体化data.dbf
        try {
            trace.createFile();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("创建文件失败");
        }

        try {
            double startTime = System.currentTimeMillis();//启动时间
            trace.getStatistics();
            double endTime = System.currentTimeMillis();//终止时间
            double runTime = endTime - startTime;//运行时长
            System.out.printf("总I/O次数: %-33s 命中率: %-34s 运行时间: %-33s\n", trace.getIOCounter(), trace.getHitRate(), runTime);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("追踪程序异常");
        }

    }
}