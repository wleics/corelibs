package zwc.com.cloverstudio.app.corelibs.application;

import android.app.Application;
import android.text.TextUtils;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import zwc.com.cloverstudio.app.corelibs.utils.PreferencesUtils;

/**
 * 上下文
 */
public class CoreApplication extends Application {

    //应用上下文
    private static CoreApplication coreContext;

    private static final Map<String, Optional<String>> cache = new HashMap<>();

    private PreferencesUtils preferencesUtils;

    private Map<String, String> cache4TypeAndUrl = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        coreContext = this;
        preferencesUtils = PreferencesUtils.getPreferencesUtils(this);
    }

    public static CoreApplication getInstance() {
        return coreContext;
    }

    public PreferencesUtils getPreferencesUtils(){
        return preferencesUtils;
    }

    public void cacheTypeAndUrl(Map<String, String> map) {
        if (map == null) {
            return;
        }
        if (cache4TypeAndUrl.size() > 0) {
            cache4TypeAndUrl.clear();
        }
        cache4TypeAndUrl.putAll(map);
    }

    public Map<String, String> getCache4TypeAndUrl() {
        return cache4TypeAndUrl;
    }

    /**
     * 缓存数据
     *
     * @param key
     * @param val
     */
    public void cacheData(Optional<String> key, Optional<String> val) {
        if (key.isPresent()) {
            cache.put(key.get(),
                      Optional.ofNullable(val.orElse("")));
        }

    }

    /**
     * 获取缓存中的数据
     *
     * @param key
     */
    public Optional<String> getCacheData(Optional<String> key) {
        if (key.isPresent()) {
            if (cache.containsKey(key.get())) {
                return cache.get(key.get());
            }
        }

        return Optional.of("");
    }

    /**
     * 缓存清空
     */
    public void cleanCache() {
        if (cache.size() > 0) {
            cache.clear();
        }
    }

    /**
     * 将数据写入文件系统
     *
     * @param key
     * @param val
     */
    public void cacheDataToDisk(Optional<String> key, Optional<String> val) {
        preferencesUtils.setValueToPreference(key.get(),
                                              val.get());
    }

    /**
     * 从文件系统中读取数据
     *
     * @param key
     * @return
     */
    public String readDataFromDisk(Optional<String> key) {
        return preferencesUtils.getStringValueByKeyName(key.get(),
                                                        "");
    }

    /**
     * 将数据保存到Preference中
     *
     * @param key
     * @param val
     */
    private void saveStringValue(String key, String val) {
        key = Optional.ofNullable(key)
                      .orElse("");
        if (TextUtils.isEmpty(key)) {
            return;
        }
        val = Optional.ofNullable(val)
                      .orElse("");
        preferencesUtils.setValueToPreference(key,
                                              val);
    }

}
