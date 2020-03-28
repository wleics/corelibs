package zwc.com.cloverstudio.app.corelibs.utils;

import java.util.Optional;

import zwc.com.cloverstudio.app.corelibs.application.CoreApplication;

/**
 * 文件系统缓存工具
 */
public class DataTool {

    public static DataTool getInstance() {
        return new DataTool();
    }

    public void cacheData(String key, String val) {
        try {
            CoreApplication appApplication = CoreApplication.getInstance();
            appApplication.cacheDataToDisk(Optional.ofNullable(key),
                                           Optional.ofNullable(val));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取缓存中的数据
     *
     * @param key
     * @return
     */
    public String getCacheData(String key) {
        try {
            CoreApplication appApplication = CoreApplication.getInstance();
            return appApplication.readDataFromDisk(Optional.ofNullable(key));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return "";
    }
}
