package com.wantee.common.log;

public class Log {
    private static ILog mLogger = null;

    public static void e(String tag, String message) {
        log(LogEnum.Error, tag, message);
    }

    public static void e(String tag, String message, Throwable e) {
        e(tag, message + "\n\r" + android.util.Log.getStackTraceString(e));
    }

    public static void w(String tag, String message) {
        log(LogEnum.Waring, tag, message);
    }

    public static void w(String tag, String message, Throwable e) {
        w(tag, message + "\n\r" + android.util.Log.getStackTraceString(e));
    }

    public static void i(String tag, String message) {
        log(LogEnum.Info, tag, message);
    }

    public static void i(String tag, String message, Throwable e) {
        i(tag, message + "\n\r" + android.util.Log.getStackTraceString(e));
    }

    public static void setLogger(ILog logger) {
        mLogger = logger;
    }

    private static void log(LogEnum level, String tag, String message) {
        if (mLogger != null) {
            mLogger.log(level, tag, message);
        }
    }
}
