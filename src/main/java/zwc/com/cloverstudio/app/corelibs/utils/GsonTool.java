package zwc.com.cloverstudio.app.corelibs.utils;

import com.google.gson.Gson;

/**
 * @ClassName:JsonTool
 * @功能：
 * @作者：wlei
 * @日期：2020/2/28-12:55 AM
 **/
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
