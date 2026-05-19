package com.ygl.strong.utils;

import android.util.Log;

/**
 * Log工具类
 * <p>
 * 建议任何打印日志的地方使用此工具类
 * 用意：软件发布后，方便屏蔽掉所有log日志打印，提高应用的安全性
 * </p>
 * <p>
 * 屏蔽方式：建议采用注释代码的方法。
 * 原因：使用布尔成员标记是否打印日志，容易遭到内存注入等手段的破解。
 * </p>
 */
public class LogUtil {

    private static final String TAG = "LogUtil";
    private static final boolean DEBUG = Constant.INSTANCE.getIS_DEBUG();

    public static void v(String msg) {
        if (DEBUG) v(TAG, msg);
    }

    public static void d(String msg) {
        if (DEBUG) d(TAG, msg);
    }

    public static void i(String msg) {
        if (DEBUG) i(TAG, msg);
    }

    public static void w(String msg) {
        if (DEBUG) w(TAG, msg);
    }

    public static void e(String msg) {
        if (DEBUG) e(TAG, msg);
    }


    public static void v(String tag, String msg) {
        if (DEBUG) Log.v(tag, msg + "");
    }

    public static void d(String tag, String msg) {
        if (DEBUG) Log.d(tag, msg + "");
    }

    public static void i(String tag, String msg) {
        if (DEBUG) Log.i(tag, msg + "");
    }

    public static void w(String tag, String msg) {
        if (DEBUG) Log.w(tag, msg + "");
    }

    public static void e(String tag, String msg) {
        if (DEBUG) Log.e(tag, msg + "");
    }


    /**
     * 产生日志文件
     */
//    public static void initDiskLog(){
//        Logger.addLogAdapter(new DiskLogAdapter());
//    }

//    public static void v(String tag, String msg) {
//        Logger.v(tag, msg + "");
//    }
//
//    public static void d(String tag, String msg) {
//        Logger.d(tag, msg + "");
//    }
//
//    public static void i(String tag, String msg) {
//        Logger.i(tag, msg + "");
//    }
//
//    public static void w(String tag, String msg) {
//        Logger.w(tag, msg + "");
//    }
//
//    public static void e(String tag, String msg) {
//        Logger.e(tag, msg + "");
//    }

}
