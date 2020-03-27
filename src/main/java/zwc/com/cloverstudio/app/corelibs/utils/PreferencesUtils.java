package zwc.com.cloverstudio.app.corelibs.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


public class PreferencesUtils {
    Context context;
    SharedPreferences mPreferences;

    private static final String PREFERENCES_FILE_NAME = "com.cloverstudio.corelibs";

    public static PreferencesUtils getPreferencesUtils(Context context) {
        PreferencesUtils preferencesUtils = new PreferencesUtils(context,
                                                                 PREFERENCES_FILE_NAME);
        return preferencesUtils;
    }

    public static PreferencesUtils getPreferencesUtils(Context context, String preferenceName) {
        PreferencesUtils preferencesUtils = new PreferencesUtils(context,
                                                                 preferenceName);
        return preferencesUtils;
    }

    private PreferencesUtils(Context context, String preferenceName) {
        this.context = context;
        getPreferenceByName(preferenceName);
    }

    /**
     * 获取SharedPreferences
     *
     * @param preferenceName
     * @return
     */
    public SharedPreferences getPreferenceByName(String preferenceName) {
        mPreferences = context.getSharedPreferences(preferenceName,
                                                    Context.MODE_PRIVATE);
        return mPreferences;
    }

    /**
     * 将值写入Preference中(字符串)
     *
     * @param keyName
     * @param value
     */
    public void setValueToPreference(String keyName, String value) {
        Editor editor = mPreferences.edit();
        editor.putString(keyName,
                         value);
        editor.commit();
    }

    /**
     * 将值写入Preference中(数字)
     *
     * @param keyName
     * @param value
     */
    public void setIntValueToPreference(String keyName, int value) {
        Editor editor = mPreferences.edit();
        editor.putInt(keyName,
                      value);
        editor.commit();
    }

    /**
     * 将值写入Preference中(布尔型)
     *
     * @param keyName
     * @param value
     */
    public void setBooleanValueToPreference(String keyName, Boolean value) {
        Editor editor = mPreferences.edit();
        editor.putBoolean(keyName,
                          value);
        editor.commit();
    }

    /**
     * 获取字符串值
     *
     * @param key
     * @param defValue
     * @return
     */
    public String getStringValueByKeyName(String key, String defValue) {
        return mPreferences.getString(key,
                                      defValue);
    }

    /**
     * 获取整数值
     *
     * @param key
     * @param defValue
     * @return
     */
    public int getIntValueByKey(String key, int defValue) {
        return mPreferences.getInt(key,
                                   defValue);
    }

    /**
     * 获取布尔型值
     *
     * @param key
     * @param defValue
     * @return
     */
    public boolean getBooleanByKey(String key, boolean defValue) {
        return mPreferences.getBoolean(key,
                                       defValue);
    }

    /**
     * 清空SharedPreferences中的数据
     */
    public void clear() {
        Editor editor = mPreferences.edit();
        editor.clear();
        editor.commit();
    }
}
