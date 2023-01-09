package edu.ustc;

import edu.ustc.buffer.BMgr;

public class Main {
    public static void main(String[] args) {
        BMgr bMgr = new BMgr();
        bMgr.fixPage(0,0);
        System.out.println("Hello world!");
    }
}