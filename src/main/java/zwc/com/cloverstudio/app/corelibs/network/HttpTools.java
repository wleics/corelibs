package zwc.com.cloverstudio.app.corelibs.network;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import zwc.com.cloverstudio.app.corelibs.thread.NamedThreadFactory;

/**
 * 网络请求封装
 */
public class HttpTools {

    //核心线程个数
    private static final int AVALIABLE_PROCESSORS = Runtime.getRuntime()
                                                           .availableProcessors();
    //构件线程池
    private static final ThreadPoolExecutor POOL_EXECUTOR =
            new ThreadPoolExecutor(AVALIABLE_PROCESSORS,
                                   10,
                                   1,
                                   TimeUnit.MINUTES,
                                   new LinkedBlockingQueue<>(5),
                                   new NamedThreadFactory("数据转换线程池"),
                                   new ThreadPoolExecutor.CallerRunsPolicy());

    /**
     * 执行一系列的http请求，并在所有请求执行完成后，返回所有请求结果
     *
     * @param httpOperates
     * @return
     */
    public static List<HttpOperateResult> executeHttpOperateBy(List<HttpOperate> httpOperates) {
        ////构建一个线程池
        List<CompletableFuture<HttpOperateResult>> operateResults = httpOperates.stream()
                                                                                .map(httpOperate -> CompletableFuture.supplyAsync(() -> httpOperate.getExecuteResult(),
                                                                                                                                  POOL_EXECUTOR))
                                                                                .collect(Collectors.toList());
        List<HttpOperateResult> results = operateResults.stream()
                                                        .map(CompletableFuture::join)
                                                        .collect(Collectors.toList());

        return results;
    }

    /**
     * 执行post请求
     *
     * @param url
     * @param params
     */
    public static String post(String url, Map<String, String> params) {

        FormBody.Builder builder = new FormBody.Builder();
        Set<Map.Entry<String, String>> entrySet = params.entrySet();

        entrySet.forEach(entry -> {
            String key = entry.getKey();
            String value = entry.getValue();
            builder.add(key,
                        value);
        });

        FormBody body = builder.build();
        OkHttpClient client = new OkHttpClient.Builder().readTimeout(5,
                                                                     TimeUnit.SECONDS)
                                                        .callTimeout(5,
                                                                     TimeUnit.SECONDS)
                                                        .build();

        Request request = new Request.Builder().url(url)
                                               .post(body)
                                               .build();

        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            String json = response.body()
                                  .string();
            return json;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 执行get请求
     *
     * @param url
     */
    public static String get(String url) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url)
                                               .get()
                                               .build();
        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            String json = response.body()
                                  .string();
            return json;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 执行post请求，已经json作为请求参数
     *
     * @param url
     * @param json
     * @param success
     * @param failure
     */
    public static void post(String url, String json, Success success, Failure failure) {

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON,
                                              json);


        try {
            executeHttpPostAsync(url,
                                 success,
                                 failure,
                                 body);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 执行post请求，以 Map<String, String>作为请求参数
     *
     * @param url
     * @param params
     * @param success
     * @param failure
     */
    public static void post(String url,
                            Map<String, String> params,
                            File file,
                            Success success,
                            Failure failure) {

        RequestBody fileBody = RequestBody.create(MediaType.parse("image/jpeg"),
                                                  file);

        MultipartBody.Builder builder = new MultipartBody.Builder().addFormDataPart("file",
                                                                                    file.getName(),
                                                                                    fileBody);


        Set<Map.Entry<String, String>> entrySet = params.entrySet();

        entrySet.forEach(entry -> {
            String key = entry.getKey();
            String value = entry.getValue();
            builder.addFormDataPart(key,
                        value);
        });

        RequestBody body = builder.build();

        try {
            executeHttpPostAsync(url,
                                 success,
                                 failure,
                                 body);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 执行post请求，以 Map<String, String>作为请求参数
     *
     * @param url
     * @param params
     * @param success
     * @param failure
     */
    public static void post(String url,
                            Map<String, String> params,
                            Success success,
                            Failure failure) {


        FormBody.Builder builder = new FormBody.Builder();
        Set<Map.Entry<String, String>> entrySet = params.entrySet();

        entrySet.forEach(entry -> {
            String key = entry.getKey();
            String value = entry.getValue();
            builder.add(key,
                        value);
        });

        FormBody body = builder.build();

        try {
            executeHttpPostAsync(url,
                                 success,
                                 failure,
                                 body);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 执行具体的Http post请求
     *
     * @param url
     * @param success
     * @param failure
     * @param body
     * @throws IOException
     */
    private static void executeHttpPostAsync(String url,
                                             Success success,
                                             Failure failure,
                                             RequestBody body) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder().readTimeout(5,
                                                                     TimeUnit.SECONDS)
                                                        .callTimeout(5,
                                                                     TimeUnit.SECONDS)
                                                        .build();

        Request request = new Request.Builder().url(url)
                                               .post(body)
                                               .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (failure != null) {
                    failure.callback(e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String data = response.body()
                                      .string();
                if (success != null) {
                    success.callback(data);
                }
            }
        });
    }

    /**
     * 执行get请求
     *
     * @param url
     * @param success
     * @param failure
     */
    public static void get(String url, Success success, Failure failure) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url)
                                               .get()
                                               .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (failure != null) {
                    failure.callback(e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String data = response.body()
                                      .string();
                if (success != null) {
                    success.callback(data);
                }
            }
        });
    }


    public interface Success extends HttpCallBack {
    }

    public interface Failure extends HttpCallBack {
    }

    @FunctionalInterface
    public interface HttpCallBack {
        void callback(String data);
    }

    /**
     * 网络请求
     */
    public static class HttpOperate {
        private int type = 0;
        private String url = "";
        private Map<String, String> param = new HashMap<>();

        public static HttpOperate getInstance(int type, String url, Map<String, String> param) {
            HttpOperate basicStockDataInfo = new HttpOperate();
            basicStockDataInfo.type = type;
            basicStockDataInfo.url = url;
            basicStockDataInfo.param = param;
            return basicStockDataInfo;
        }

        public HttpOperateResult getExecuteResult() {

            String json = "";
            if (param != null) {
                json = HttpTools.post(url,
                                      param);
            } else {
                json = HttpTools.get(url);
            }

            HttpOperateResult httpOperateResult = new HttpOperateResult(type,
                                                                        json);

            return httpOperateResult;
        }
    }

    /**
     * 网络请求结果
     */
    public static class HttpOperateResult {
        private int type = 0;
        private String jsonData = "";

        public HttpOperateResult(int type, String jsonData) {
            this.type = type;
            this.jsonData = jsonData;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getJsonData() {
            return jsonData;
        }

        public void setJsonData(String jsonData) {
            this.jsonData = jsonData;
        }
    }
}
