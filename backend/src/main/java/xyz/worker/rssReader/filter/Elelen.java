package xyz.worker.rssReader.filter;

import lombok.Data;
import org.springframework.stereotype.Component;

public class Elelen {
    private static int lay=10;
    private static int ele=8;
    private static int LAYERS=1 << lay;
    private static int ELEMS=1 << ele;
    private static int LASTELEMS=1 << (ele+1);
    private static int  CAPCITY=1 << (lay+ele);
    private static int LASTCAPCITY=1 << (lay+ele+1);
    private static int QUEUECAP=3;

    public static int getLAYERS() {
        return LAYERS;
    }

    public static int getELEMS() {
        return ELEMS;
    }

    public static int getLASTELEMS() {
        return LASTELEMS;
    }

    public static int getCAPCITY() {
        return CAPCITY;
    }

    public static int getLASTCAPCITY() {
        return LASTCAPCITY;
    }

    public static int getQUEUECAP() {
        return QUEUECAP;
    }
}
