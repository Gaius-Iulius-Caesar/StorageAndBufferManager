package edu.ustc;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Trace trace = new Trace();
        // 具体化data.dbf
        try {
            trace.createFile();
            System.out.println("data.dbf初始化完成...");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("data.dbf初始化失败");
        }

        double startTime = System.currentTimeMillis();//启动时间
        trace.getStatistics();
        double endTime = System.currentTimeMillis();//终止时间
        double runTime = endTime - startTime;//运行时长
        System.out.printf("============================\n追踪程序运行完成, 最终数据为:\n总I/O次数: %s\t命中率: %s\t运行时间: %sms\n", trace.getIOCounter(), trace.getHitRate(), runTime);

    }
}