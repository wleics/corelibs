package zwc.com.cloverstudio.app.corelibs.utils;

import android.os.Build;
import android.util.Log;

import java.time.LocalDateTime;

import zwc.com.cloverstudio.app.corelibs.BuildConfig;


/**
 * 系统工具
 */
public class LogUtils {
    private static final String tag = "com.studio.corelib";


    /**
     * 日志打印
     *
     * @param msg
     */
    public static void log(Object msg) {
        boolean printAppLogger = BuildConfig.PRINT_APP_LOGGER;
        msg = msg == null ? "" : msg;
        if (printAppLogger) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.e(tag,
                      LocalDateTime.now() + "：" + msg);
            } else {
                Log.e(tag,
                      msg.toString());
            }
        }
    }

    /**
     * 打印标签
     *
     * @param tag
     * @param count
     */
    public static void logTag(String tag, int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(tag);
        }
        Log.i(tag,
              builder.toString());
    }


}
