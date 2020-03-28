package zwc.com.cloverstudio.app.corelibs.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonSyntaxException;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.popup.QMUIPopup;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import butterknife.ButterKnife;
import zwc.com.cloverstudio.app.corelibs.R;
import zwc.com.cloverstudio.app.corelibs.application.CoreApplication;
import zwc.com.cloverstudio.app.corelibs.consts.CoreConsts;
import zwc.com.cloverstudio.app.corelibs.network.HttpTools;
import zwc.com.cloverstudio.app.corelibs.utils.DataTool;
import zwc.com.cloverstudio.app.corelibs.utils.DialogUtils;
import zwc.com.cloverstudio.app.corelibs.utils.GsonTool;
import zwc.com.cloverstudio.app.corelibs.utils.JsonTools;
import zwc.com.cloverstudio.app.corelibs.utils.LogUtils;
import zwc.com.cloverstudio.app.corelibs.utils.PermissionsTool;
import zwc.com.cloverstudio.app.corelibs.utils.PreferencesUtils;

/**
 * 基础类
 */
public abstract class CoreActivity extends AppCompatActivity {

    private static final int DISMISS_HUD = 999;
    private static final int HTTP_SUCCESS = 998;
    private static final int HTTP_FAILURE = 997;

    private Map<String, Optional<HttpTools.Success>> successMap = new HashMap<>();
    private Map<String, Optional<HttpTools.Failure>> failureMap = new HashMap<>();

    private NavigationBar navigationBar;

    private int mRequestCode;

    private DialogUtils dialogUtils;

    private JsonTools jsonTools;

    private QMUIPopup mNormalPopup;
    private TextView popupContentView;
    private Toast mToast;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isSwipeToDismiss()) {
            swipeToDismiss();
        }
        setContentView(R.layout.core_activity_base);

        //根视图
        LinearLayout root = findViewById(R.id.root_container);

        //子视图
        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                              ViewGroup.LayoutParams.MATCH_PARENT);
        View child = getLayoutInflater().inflate(getView(),
                                                 root,
                                                 false);
        root.addView(child,
                     layoutParams);

        //初始化导航栏
        navigationBar = new NavigationBar(this);
        //设置默认的返回事件
        navigationBar.setNavigationBackBtnOnClick(v -> {
            finish();
        });
        //隐藏底部的线条
        navigationBar.hideBottomLine();

        Bundle bundle = Optional.ofNullable(getIntent().getExtras())
                                .orElse(new Bundle());
        String title = bundle.getString(CoreConsts.NAV_TITLE,
                                        "");
        setNavigationTitle(title);
        ButterKnife.bind(this);
        initView();
    }

    private void swipeToDismiss() {
        SlidrConfig config = getSlidrConfig();
        Slidr.attach(this,
                     config);
    }

    private SlidrConfig getSlidrConfig() {
        return new SlidrConfig.Builder().position(SlidrPosition.LEFT)
                                        .sensitivity(1f)
                                        .scrimColor(Color.BLACK)
                                        .scrimStartAlpha(0.8f)
                                        .scrimEndAlpha(0f)
                                        .velocityThreshold(2400)
                                        .distanceThreshold(0.25f)
                                        .edge(true | false)
                                        .edgeSize(0.18f)
                                        .build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        QMUIStatusBarHelper.setStatusBarLightMode(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()){
            destroy();
        }
    }

    //@Override
    //protected void onDestroy() {
    //    super.onDestroy();
    //    destroy();
    //}

    private boolean isDestroyed = false;

    private void destroy() {
        if (isDestroyed) {
            return;
        }
        //执行销毁任务
        runDestroy();
        isDestroyed = true;
    }

    /**
     * 是否启用滑动关闭
     *
     * @return
     */
    public boolean isSwipeToDismiss() {
        return true;
    }

    /**
     * 关闭时是否使用动画
     *
     * @return
     */
    public boolean finishSelfUseAnim() {
        return true;
    }

    /**
     * 启动新的activity时是否使用动画
     *
     * @return
     */
    public boolean openNextActivityUseAnim() {
        return true;
    }

    public abstract int getView();

    public abstract void initView();

    public abstract void hander4ActivityResult(int requestCode, Intent data);

    public abstract void runDestroy();

    public String getDefNavTitle() {
        // TODO: 2020/3/27 此处最好重写
        return "";
    }


    public List<HttpTools.HttpOperateResult> executeHttpOperateBy(List<HttpTools.HttpOperate> httpOperates) {
        return HttpTools.executeHttpOperateBy(httpOperates);
    }

    /**
     * 执行http请求，非异步执行
     *
     * @param url
     * @param params
     * @return
     */
    public String httpPost(String url, Map<String, String> params) {
        return HttpTools.post(url,
                              params);
    }

    /**
     * 异步http-post请求
     *
     * @param url
     * @param json
     * @param hudMessage
     * @param success
     * @param failure
     */
    public void httpPostAsync(String url,
                              String json,
                              String hudMessage,
                              HttpTools.Success success,
                              HttpTools.Failure failure) {

        final UUID uuid = UUID.randomUUID();

        successMap.put(uuid.toString(),
                       Optional.ofNullable(success));
        failureMap.put(uuid.toString(),
                       Optional.ofNullable(failure));


        Optional<String> optionalS = Optional.ofNullable(hudMessage);

        optionalS.ifPresent((message) -> {
            getDialogUtils().showHUD(message);
        });
        //执行请求
        HttpTools.post(url,
                       json,
                       (String responseBody) -> {
                           //关闭hud
                           optionalS.ifPresent((message) -> {
                               mHandler.sendEmptyMessage(DISMISS_HUD);
                           });

                           Message message = getMessage(responseBody,
                                                        HTTP_SUCCESS,
                                                        uuid.toString());
                           mHandler.sendMessage(message);

                       },
                       (String failureMessage) -> {
                           //关闭hud
                           optionalS.ifPresent((message) -> {
                               mHandler.sendEmptyMessage(DISMISS_HUD);
                           });

                           Message message = getMessage(failureMessage,
                                                        HTTP_FAILURE,
                                                        uuid.toString());
                           mHandler.sendMessage(message);

                       });
    }

    public void httpPostAsync4AppUpdate(String url,
                                        Map<String, String> params,
                                        String hudMessage,
                                        HttpTools.Success success,
                                        HttpTools.Failure failure) {

        final UUID uuid = UUID.randomUUID();

        successMap.put(uuid.toString(),
                       Optional.ofNullable(success));
        failureMap.put(uuid.toString(),
                       Optional.ofNullable(failure));


        Optional<String> optionalS = Optional.ofNullable(hudMessage);

        optionalS.ifPresent((message) -> {
            getDialogUtils().showHUD(message);
        });
        String finalUrl = url;
        mHandler.postDelayed(new Runnable() {
                                 @Override
                                 public void run() {
                                     //执行请求
                                     HttpTools.post(finalUrl,
                                                    params,
                                                    (String responseBody) -> {
                                                        //关闭hud
                                                        optionalS.ifPresent((message) -> {
                                                            mHandler.sendEmptyMessage(DISMISS_HUD);
                                                        });

                                                        Message message = getMessage(responseBody,
                                                                                     HTTP_SUCCESS,
                                                                                     uuid.toString());
                                                        mHandler.sendMessage(message);

                                                    },
                                                    (String failureMessage) -> {
                                                        //关闭hud
                                                        optionalS.ifPresent((message) -> {
                                                            mHandler.sendEmptyMessage(DISMISS_HUD);
                                                        });

                                                        Message message = getMessage(failureMessage,
                                                                                     HTTP_FAILURE,
                                                                                     uuid.toString());
                                                        mHandler.sendMessage(message);

                                                    });
                                 }
                             },
                             500);
    }

    /**
     * 异步http-post请求
     *
     * @param url
     * @param params
     * @param hudMessage
     * @param success
     * @param failure
     */
    public void httpPostAsync(String url,
                              Map<String, String> params,
                              String hudMessage,
                              HttpTools.Success success,
                              HttpTools.Failure failure) {


        final UUID uuid = UUID.randomUUID();

        successMap.put(uuid.toString(),
                       Optional.ofNullable(success));
        failureMap.put(uuid.toString(),
                       Optional.ofNullable(failure));


        Optional<String> optionalS = Optional.ofNullable(hudMessage);

        optionalS.ifPresent((message) -> {
            getDialogUtils().showHUD(message);
        });
        String finalUrl = url;
        mHandler.postDelayed(new Runnable() {
                                 @Override
                                 public void run() {
                                     //执行请求
                                     HttpTools.post(finalUrl,
                                                    params,
                                                    (String responseBody) -> {
                                                        //关闭hud
                                                        optionalS.ifPresent((message) -> {
                                                            mHandler.sendEmptyMessage(DISMISS_HUD);
                                                        });

                                                        printServerResult(responseBody,
                                                                          url);

                                                        Message message = getMessage(responseBody,
                                                                                     HTTP_SUCCESS,
                                                                                     uuid.toString());
                                                        mHandler.sendMessage(message);

                                                    },
                                                    (String failureMessage) -> {
                                                        //关闭hud
                                                        optionalS.ifPresent((message) -> {
                                                            mHandler.sendEmptyMessage(DISMISS_HUD);
                                                        });

                                                        Message message = getMessage(failureMessage,
                                                                                     HTTP_FAILURE,
                                                                                     uuid.toString());
                                                        mHandler.sendMessage(message);

                                                    });
                                 }
                             },
                             500);

    }

    private void printServerResult(String responseBody, String url) {
        //打印接口返回的数据
        StringBuilder sb = new StringBuilder();
        sb.append("==========\n");
        sb.append("接口:" + url + "\n");
        sb.append("正常返回:\n" + responseBody);
        sb.append("==========");
        printLog(sb);
    }

    /**
     * 异步http-post请求
     *
     * @param url
     * @param params
     * @param hudMessage
     * @param success
     * @param failure
     */
    public void httpPostAsync(String url,
                              Map<String, String> params,
                              File file,
                              String hudMessage,
                              HttpTools.Success success,
                              HttpTools.Failure failure) {


        final UUID uuid = UUID.randomUUID();

        successMap.put(uuid.toString(),
                       Optional.ofNullable(success));
        failureMap.put(uuid.toString(),
                       Optional.ofNullable(failure));


        Optional<String> optionalS = Optional.ofNullable(hudMessage);

        optionalS.ifPresent((message) -> {
            getDialogUtils().showHUD(message);
        });
        String finalUrl = url;
        mHandler.postDelayed(new Runnable() {
                                 @Override
                                 public void run() {
                                     //执行请求
                                     HttpTools.post(finalUrl,
                                                    params,
                                                    file,
                                                    (String responseBody) -> {
                                                        //关闭hud
                                                        optionalS.ifPresent((message) -> {
                                                            mHandler.sendEmptyMessage(DISMISS_HUD);
                                                        });

                                                        printServerResult(responseBody,
                                                                          url);

                                                        Message message = getMessage(responseBody,
                                                                                     HTTP_SUCCESS,
                                                                                     uuid.toString());
                                                        mHandler.sendMessage(message);

                                                    },
                                                    (String failureMessage) -> {
                                                        //关闭hud
                                                        optionalS.ifPresent((message) -> {
                                                            mHandler.sendEmptyMessage(DISMISS_HUD);
                                                        });

                                                        Message message = getMessage(failureMessage,
                                                                                     HTTP_FAILURE,
                                                                                     uuid.toString());
                                                        mHandler.sendMessage(message);

                                                    });
                                 }
                             },
                             500);

    }

    /**
     * 执行get请求
     *
     * @param url
     * @param hudMessage
     * @param success
     * @param failure
     */
    public void httpGetAsync(String url,
                             String hudMessage,
                             HttpTools.Success success,
                             HttpTools.Failure failure) {

        final UUID uuid = UUID.randomUUID();

        successMap.put(uuid.toString(),
                       Optional.ofNullable(success));
        failureMap.put(uuid.toString(),
                       Optional.ofNullable(failure));


        Optional<String> optionalS = Optional.ofNullable(hudMessage);

        optionalS.ifPresent((message) -> {
            getDialogUtils().showHUD(message);
        });

        HttpTools.get(url,
                      (String responseBody) -> {

                          //关闭hud
                          optionalS.ifPresent((message) -> {
                              mHandler.sendEmptyMessage(DISMISS_HUD);
                          });

                          printServerResult(responseBody,
                                            url);

                          Message message = getMessage(responseBody,
                                                       HTTP_SUCCESS,
                                                       uuid.toString());
                          mHandler.sendMessage(message);

                      },
                      (String failureMessage) -> {
                          //关闭hud
                          optionalS.ifPresent((message) -> {
                              mHandler.sendEmptyMessage(DISMISS_HUD);
                          });

                          Message message = getMessage(failureMessage,
                                                       HTTP_FAILURE,
                                                       uuid.toString());
                          mHandler.sendMessage(message);

                      });
    }

    /**
     * 打印日志
     *
     * @param msg
     */
    public void printLog(Object msg) {
        LogUtils.log(msg);
    }


    /**
     * 显示Toast
     *
     * @param msg
     */
    public void showToast(CharSequence msg) {
        msg = msg == null ? "" : msg;
        try {
            if (mToast != null) {
                mToast.cancel();
            }

            mToast = Toast.makeText(getApplicationContext(),
                                    null,
                                    Toast.LENGTH_SHORT);
            mToast.setText(msg);
            mToast.setGravity(Gravity.CENTER,
                              0,
                              0);
            mToast.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示AlertDialog
     *
     * @param content
     * @param sureBtnLabel
     * @param cancelBtnLabel
     * @param cancelBtnOnClick
     * @param sureBtnOnClick
     */
    public void showAlertDialog(String content,
                                String sureBtnLabel,
                                String cancelBtnLabel,
                                DialogUtils.OnBtnClick cancelBtnOnClick,
                                DialogUtils.OnBtnClick sureBtnOnClick) {
        getDialogUtils().showNormalAlertDialog(content,
                                               sureBtnLabel,
                                               cancelBtnLabel,
                                               cancelBtnOnClick,
                                               sureBtnOnClick);
    }

    /**
     * 显示AlertDialog
     *
     * @param title
     * @param content
     * @param sureBtnLabel
     * @param cancelBtnLabel
     * @param cancelBtnOnClick
     * @param sureBtnOnClick
     */
    public void showAlertDialog(String title,
                                String content,
                                String sureBtnLabel,
                                String cancelBtnLabel,
                                DialogUtils.OnBtnClick cancelBtnOnClick,
                                DialogUtils.OnBtnClick sureBtnOnClick) {
        getDialogUtils().showNormalAlertDialog(title,
                                               content,
                                               sureBtnLabel,
                                               cancelBtnLabel,
                                               cancelBtnOnClick,
                                               sureBtnOnClick);

    }

    /**
     * 显示AlertDialog
     *
     * @param title
     * @param content
     * @param operates
     */
    public void showAlertDialog(String title, String content, List<DialogUtils.Operate> operates) {
        getDialogUtils().showAlertDialog(title,
                                         content,
                                         operates);
    }

    /**
     * 显示成功提醒
     *
     * @param tip
     */
    public void showSuccessTip(String tip) {
        getDialogUtils().showSuccessTip(tip);
    }

    /**
     * 显示失败提醒
     *
     * @param tip
     */
    public void showFailTip(String tip) {
        getDialogUtils().showFailTip(tip);
    }

    /**
     * 显示消息提醒
     *
     * @param tip
     */
    public void showInfoTip(String tip) {
        getDialogUtils().showInfoTip(tip);
    }

    /**
     * 获取一个自定义显示内容的Dialog
     *
     * @param layoutResId
     * @param title
     * @return
     */
    public Dialog getCustomDialog(@LayoutRes int layoutResId, String title) {
        title = Optional.ofNullable(title)
                        .orElse("");
        return getDialogUtils().getCustomDialog(layoutResId,
                                                title);
    }

    /**
     * 获取一个自定义显示内容的Dialog
     *
     * @param layoutResId
     * @param title
     * @param sureBtnLabel
     * @param sureBtnOnClick
     * @return
     */
    public Dialog getCustomDialog(@LayoutRes int layoutResId,
                                  String title,
                                  String sureBtnLabel,
                                  DialogUtils.CustomDialogOnBtnClick sureBtnOnClick) {
        return getDialogUtils().getCustomDialog(layoutResId,
                                                title,
                                                sureBtnLabel,
                                                "取消",
                                                sureBtnOnClick,
                                                null);
    }

    /**
     * 获取一个自定义dialog，不需要任何按钮
     *
     * @param layoutResId
     * @param title
     * @return
     */
    public Dialog getCustomDialogNoBtn(@LayoutRes int layoutResId, String title) {
        return getDialogUtils().getCustomDialog(layoutResId,
                                                title,
                                                null,
                                                null,
                                                null,
                                                null);
    }


    /**
     * 将json字符串转换成对的对象类型
     *
     * @param json     json字符串
     * @param classOfT 对象类型
     * @param <T>
     * @return
     */
    public <T> Optional<T> fromJson2Obj(String json, Class<T> classOfT) {
        try {
            Optional<T> result = Optional.ofNullable(getJsonTools().fromJson2Obj(json,
                                                                                 classOfT));
            return result;
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            showToast("网络连接出错，请稍后重试！");
        }

        return Optional.empty();
    }

    /**
     * 将给定的json字符串装换成List
     *
     * @param json
     * @param <T>
     * @return
     * @throws JsonSyntaxException
     */
    public <T> List<T> fromJson2List(String json, Class<T> classOfT) {
        try {
            return getJsonTools().fromJson2List(json,
                                                classOfT);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();

    }

    /**
     * 针对使用Expose注解对象，将该对象转换成json字符串
     *
     * @param obj
     * @return
     */
    public String toJsonByExpose(Object obj) {
        return getJsonTools().toJsonByExpose(obj);
    }

    /**
     * 启动新的Activity
     *
     * @param action
     */
    public void startActivityBy(String action) {
        startActivityBy(action,
                        null,
                        null);
    }

    /**
     * 启动新的Activity
     *
     * @param action
     * @param title
     */
    public void startActivityBy(String action, String title) {
        startActivityBy(action,
                        title,
                        null);
    }

    /**
     * 启动新的Activity
     *
     * @param action
     * @param bundle
     */
    public void startActivityBy(String action, Bundle bundle) {
        startActivityBy(action,
                        "",
                        bundle);
    }

    /**
     * 启动新的Activity
     *
     * @param action
     * @param title
     * @param bundle
     */
    public void startActivityBy(String action, String title, Bundle bundle) {
        startActivityBy(action,
                        title,
                        bundle,
                        -1);

    }

    /**
     * 启动activity
     *
     * @param action
     * @param title
     * @param bundle
     * @param requestCode
     */
    public void startActivityBy(String action, String title, Bundle bundle, int requestCode) {

        action = Optional.ofNullable(action)
                         .orElse("");

        title = Optional.ofNullable(title)
                        .orElse(getDefNavTitle());

        if (TextUtils.isEmpty(action.trim())) {
            showFailTip("action地址不存在");
            return;
        }

        Intent intent = new Intent(action);
        bundle = Optional.ofNullable(bundle)
                         .orElse(new Bundle());
        bundle.putString(CoreConsts.NAV_TITLE,
                         title);

        intent.putExtras(bundle);
        try {
            if (requestCode != -1) {
                mRequestCode = requestCode;
                startActivityForResult(intent,
                                       requestCode);
            } else {
                startActivity(intent);
            }
            if (openNextActivityUseAnim()) {
                overridePendingTransition(R.anim.slid_left_in,
                                          R.anim.slid_right_out);
            }
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            showToast("无法启动：" + action);
        }

    }

    private Handler mHandler = new Handler((Message msg) -> {
        if (msg.what == DISMISS_HUD) {
            getDialogUtils().hudDismiss();
        } else if (msg.what == HTTP_SUCCESS) {
            String data = (String) msg.getData()
                                      .get("data");
            String serialNumber = (String) msg.getData()
                                              .get(CoreConsts.REQUEST_SERIAL_NUMBER);
            Optional<HttpTools.Success> optionalSuccess = successMap.get(serialNumber);
            optionalSuccess.ifPresent((success -> {
                success.callback(data);
            }));
            successMap.remove(serialNumber);
        } else if (msg.what == HTTP_FAILURE) {
            String data = (String) msg.getData()
                                      .get("data");
            String serialNumber = (String) msg.getData()
                                              .get(CoreConsts.REQUEST_SERIAL_NUMBER);
            Optional<HttpTools.Failure> optionalFailure = failureMap.get(serialNumber);
            optionalFailure.ifPresent((failure -> {
                failure.callback(data);
            }));
            failureMap.remove(serialNumber);
        }
        return false;
    });

    /**
     * 执行数据缓存
     *
     * @param key
     * @param val
     */
    public void cacheDataInMemory(String key, String val) {
        try {
            CoreApplication appApplication = CoreApplication.getInstance();
            appApplication.cacheData(Optional.ofNullable(key),
                                     Optional.ofNullable(val));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将数据存于文件系统中
     *
     * @param key
     * @param val
     */
    public void cacheDataInDisk(String key, String val) {
        DataTool instance = DataTool.getInstance();
        instance.cacheData(key,
                           val);
    }

    /**
     * 获取缓存中的数据
     *
     * @param key
     * @return
     */
    public String getCacheDataInMemory(String key) {
        try {
            CoreApplication appApplication = CoreApplication.getInstance();
            return appApplication.getCacheData(Optional.ofNullable(key))
                                 .orElse("");
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 从文件系统中读取内容
     *
     * @param key
     * @return
     */
    public String getCacheDataInDisk(String key) {
        DataTool instance = DataTool.getInstance();
        return instance.getCacheData(key);
    }

    /**
     * 清空缓存
     */
    public void clearCacheInMemory() {
        CoreApplication.getInstance()
                       .cleanCache();
    }

    /**
     * 将key所指定的内容，从文件系统中去除
     *
     * @param key
     */
    public void clearCacheInDisk(String key) {
        DataTool.getInstance()
                .cacheData(key,
                           "");
    }

    /**
     * 隐藏导航栏
     */
    public void hideNavigationBar() {
        navigationBar.hideNavigationBar();
    }

    /**
     * 隐藏导航栏标题
     */
    public void hideNavigationTitle() {
        navigationBar.hideNavigationTitle();
    }

    /**
     * 隐藏导航栏右侧按钮
     */
    public void hideNavigationRightTitle() {
        navigationBar.hideNavigationRightTitle();
    }

    /**
     * 隐藏导航栏返回按钮
     */
    public void hideNavigationBackBtn() {
        navigationBar.hideNavigationBackBtn();
    }

    /**
     * 设置导航栏标题
     *
     * @param title
     */
    public void setNavigationTitle(String title) {
        navigationBar.setNavigationTitle(title);
    }

    /**
     * 设置导航栏右侧标题
     *
     * @param title
     */
    public void setNavigationRightTitle(String title) {
        navigationBar.setNavigationRightTitle(title);
    }

    /**
     * 设置导航栏右侧按钮的颜色
     *
     * @param color
     */
    public void setNavigationRightTitleColor(int color) {
        navigationBar.setNavigationRightTitleColor(color);
    }

    /**
     * 导航栏右侧标题被点击
     *
     * @param onClickListener
     */
    public void setNavigationRightTitleOnClick(View.OnClickListener onClickListener) {
        navigationBar.setNavigationRightTitleOnClick(onClickListener);
    }

    /**
     * 设置导航栏返回按钮标题
     *
     * @param title
     */
    public void setNavigationBackBtnTitle(String title) {
        navigationBar.setNavigationBackBtnTitle(title);
    }

    /**
     * 设置导航栏返回按钮的点击事件
     *
     * @param onClickListener
     */
    public void setNavigationBackBtnOnClick(View.OnClickListener onClickListener) {
        navigationBar.setNavigationBackBtnOnClick(onClickListener);
    }

    /**
     * 隐藏底部的线条
     */
    public void hideNavBottomLine() {
        navigationBar.hideBottomLine();
    }

    /**
     * 显示底部的线条
     */
    public void showNavBottomLine() {
        navigationBar.showBottomLine();
    }

    public void setNavigationBarRightImg(int resid) {
        navigationBar.setNavigationBarRightImg(resid);
    }

    public void setNavigationBarRightImgOnClick(View.OnClickListener onClickListener) {
        navigationBar.setNavigationBarRightImgOnClick(onClickListener);
    }

    public void setNavigationLeftPlusBtnTitle(String title) {
        navigationBar.setNavigationLeftPlusBtnTitle(title);
    }

    public void setNavigationLeftPlusBtnOnClick(View.OnClickListener onClickListener) {
        navigationBar.setNavigationLeftPlusBtnOnClick(onClickListener);
    }

    public void setNavigationLeftPlusBtnVisibility(int visibility) {
        navigationBar.setNavigationLeftPlusBtnVisibility(visibility);
    }


    /**
     * 显示弹出视图
     *
     * @param fromView
     * @param text
     */
    public void showPopupView(View fromView, String text) {
        popupContentView.setText(text);
        mNormalPopup.show(fromView);
    }


    /**
     * 获取Message
     *
     * @param dataStr
     * @param what
     * @return
     */
    private Message getMessage(String dataStr, int what, String serialNumber) {
        Message message = new Message();
        message.what = what;
        Bundle data = new Bundle();
        data.putString(CoreConsts.DATA,
                       dataStr);
        data.putString(CoreConsts.REQUEST_SERIAL_NUMBER,
                       serialNumber);
        message.setData(data);
        return message;
    }

    /**
     * 获取Dialog操作工具类
     *
     * @return
     */
    public DialogUtils getDialogUtils() {
        if (dialogUtils == null) {
            dialogUtils = DialogUtils.newInstance(this);
        }
        return dialogUtils;
    }

    /**
     * 获取json操作工具
     *
     * @return
     */
    private JsonTools getJsonTools() {
        if (jsonTools == null) {
            jsonTools = JsonTools.getInstance();
        }
        return jsonTools;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode,
                               resultCode,
                               data);
        if (resultCode == Activity.RESULT_OK && requestCode == mRequestCode) {
            hander4ActivityResult(requestCode,
                                  data);
        }
    }

    @Override
    public void finish() {
        super.finish();
        if (finishSelfUseAnim()) {
            overridePendingTransition(R.anim.slid_left_in_2,
                                      R.anim.slid_right_out_2);
        }
    }

    /**
     * 发送事件
     *
     * @param o
     */
    public void postEvent(Object o) {
        EventBus.getDefault()
                .post(o);

    }


    /**
     * 获取app的使用次数
     *
     * @return
     */
    public int appUseCount() {
        PreferencesUtils preferencesUtils = PreferencesUtils.getPreferencesUtils(this);
        //应用的使用次数
        int useCount = preferencesUtils.getIntValueByKey("useCount",
                                                         0);
        return useCount;
    }

    /**
     * 增加app的使用次数
     */
    public void increaseAppUseCount() {
        PreferencesUtils preferencesUtils = PreferencesUtils.getPreferencesUtils(this);
        //应用的使用次数
        int useCount = preferencesUtils.getIntValueByKey("useCount",
                                                         0);
        preferencesUtils.setIntValueToPreference("useCount",
                                                 useCount + 1);
    }

    /**
     * 用于消费json
     *
     * @param json
     * @param classOfT
     * @param action
     * @param <T>
     */
    public <T> void hander4JsonResult(String json, Class<T> classOfT, Consumer<? super T> action) {
        if (TextUtils.isEmpty(json)) {
            return;
        }
        T obj = GsonTool.getObjFromJson(json,
                                        classOfT);
        if (obj == null) {
            showToast("接口异常，请稍后重试！");
        } else {
            action.accept(obj);
        }
    }

    /**
     * 装载权限
     */
    public void installPermissions(String... permissions) {
        installPermissions(aBoolean -> {
                               if (aBoolean) {
                                   printLog("全部通过");
                               } else {
                                   printLog("没用通过，或仅通过部分权限");
                               }
                           },
                           permissions);
    }

    /**
     * 装载权限
     *
     * @param permissions
     * @param consumer    回调
     */
    public void installPermissions(io.reactivex.functions.Consumer<Boolean> consumer,
                                   String... permissions) {
        //RxPermissions rxPermissions = new RxPermissions(this);
        //rxPermissions.request(permissions)
        //             .subscribe(consumer);
        PermissionsTool.installPermissions(this,
                                           consumer,
                                           permissions);
    }


    /**
     * 导航栏封装
     */
    public static class NavigationBar {
        private View navigation_bar;
        private TextView navigation_title;
        private TextView navigation_right_title;
        private Button navigation_back_btn;
        private Activity mActivity;
        private View navigation_back_btn_img;
        private View nav_bottom_line;
        private ImageView navigation_right_img_btn;
        private Button navigation_left_btn_plus;

        private NavigationBar(Activity context) {
            mActivity = context;
            navigation_bar = context.findViewById(R.id.navigation_bar);
            navigation_title = context.findViewById(R.id.navigation_title);
            navigation_right_title = context.findViewById(R.id.navigation_right_title);
            navigation_back_btn = context.findViewById(R.id.navigation_back_btn);
            navigation_back_btn_img = context.findViewById(R.id.navigation_back_btn_img);
            nav_bottom_line = context.findViewById(R.id.nav_bottom_line);
            navigation_right_img_btn = context.findViewById(R.id.navigation_right_img_btn);
            navigation_left_btn_plus = context.findViewById(R.id.navigation_left_btn_plus);
        }

        private void setNavigationBarRightImg(int resid) {
            navigation_right_img_btn.setImageResource(resid);
            navigation_right_img_btn.setVisibility(View.VISIBLE);
        }

        private void setNavigationBarRightImgOnClick(View.OnClickListener onClickListener) {
            navigation_right_img_btn.setOnClickListener(onClickListener);
        }

        private void hideNavigationBar() {
            navigation_bar.setVisibility(View.GONE);
        }

        private void hideNavigationTitle() {
            navigation_title.setVisibility(View.INVISIBLE);
        }

        private void hideNavigationRightTitle() {
            navigation_right_title.setVisibility(View.INVISIBLE);
        }

        private void hideNavigationBackBtn() {
            navigation_back_btn.setVisibility(View.INVISIBLE);
            navigation_back_btn_img.setVisibility(View.GONE);
        }

        private void setNavigationTitle(String title) {
            title = Optional.ofNullable(title)
                            .orElse("");
            navigation_title.setText(title);
        }

        private void setNavigationRightTitle(String title) {
            title = Optional.ofNullable(title)
                            .orElse("");
            navigation_right_title.setText(title);
        }

        private void setNavigationRightTitleColor(int color) {
            navigation_right_title.setTextColor(color);
        }

        private void setNavigationRightTitleOnClick(View.OnClickListener onClickListener) {
            navigation_right_title.setOnClickListener(onClickListener);
        }

        private void setNavigationBackBtnTitle(String title) {
            title = Optional.ofNullable(title)
                            .orElse("");
            navigation_back_btn.setText(title);
        }

        private void setNavigationBackBtnOnClick(View.OnClickListener onClickListener) {
            navigation_back_btn.setOnClickListener(onClickListener);
            navigation_back_btn_img.setOnClickListener(onClickListener);
        }

        private void hideBottomLine() {
            nav_bottom_line.setVisibility(View.GONE);
        }

        private void showBottomLine() {
            nav_bottom_line.setVisibility(View.VISIBLE);
        }

        public void setNavigationLeftPlusBtnTitle(String title) {
            if (!TextUtils.isEmpty(title)) {
                navigation_left_btn_plus.setText(title);
            }
        }

        public void setNavigationLeftPlusBtnOnClick(View.OnClickListener onClickListener) {
            if (onClickListener != null) {
                navigation_left_btn_plus.setOnClickListener(onClickListener);
            }
        }

        public void setNavigationLeftPlusBtnVisibility(int visibility) {
            navigation_left_btn_plus.setVisibility(visibility);
        }

    }

}
