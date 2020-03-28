package zwc.com.cloverstudio.app.corelibs.utils;

import com.google.gson.Gson;

/**
 * Gson工具
 */
public class GsonTool {
    public static <T> T getObjFromJson(String json, Class<T> classOfT) {
        try {
            T obj = new Gson().fromJson(json,
                                        classOfT);
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }
}
