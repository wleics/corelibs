package zwc.com.cloverstudio.app.corelibs.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JsonTools {

    public static JsonTools getInstance() {
        return new JsonTools();
    }

    private JsonTools() {
    }

    /**
     * 将json字符串转换成对象
     *
     * @param json
     * @param classOfT
     * @param <T>
     * @return
     */
    public <T> T fromJson2Obj(String json, Class<T> classOfT) throws JsonSyntaxException {
        Gson gson = new Gson();
        return gson.fromJson(json,
                             classOfT);
    }

    /**
     * 将字符串转换成列表
     *
     * @param json
     * @param <T>
     * @return
     * @throws JsonSyntaxException
     */
    public <T> List<T> fromJson2List(String json, Class<T> classOfT) throws JsonSyntaxException {

        json = Optional.ofNullable(json)
                       .orElse("")
                       .trim();
        if ("".equals(json)) {
            return new ArrayList<>();
        } else {
            List<T> tmp = new Gson().fromJson(json,
                                              new TypeToken<List<T>>() {
                                              }.getType());
            Gson gson = new Gson();
            List<T> list = tmp.stream()
                              .map(t -> {
                                  String str = gson.toJson(t);
                                  T obj = gson.fromJson(str,
                                                        classOfT);
                                  return obj;
                              })
                              .collect(Collectors.toList());
            return list;
        }

    }

    /**
     * 将对象转换成json
     *
     * @param obj
     * @return
     */
    public String toJson(Object obj) {
        obj = Optional.ofNullable(obj)
                      .orElse("");
        Gson gson = new Gson();
        return gson.toJson(obj);
    }

    /**
     * 针对使用Expose注解对象，将该对象转换成json字符串
     *
     * @param obj
     * @return
     */
    public String toJsonByExpose(Object obj) {
        obj = Optional.ofNullable(obj)
                      .orElse("");

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                                     .create();
        return gson.toJson(obj);

    }
}
