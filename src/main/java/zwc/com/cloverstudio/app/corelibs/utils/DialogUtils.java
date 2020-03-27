package zwc.com.cloverstudio.app.corelibs.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import androidx.annotation.LayoutRes;

import com.kaopiz.kprogresshud.KProgressHUD;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * 对话框工具类
 */
public class DialogUtils {
    private Context mContext;
    private KProgressHUD hud;
    QMUITipDialog tipDialog;

    private int mCurrentDialogStyle = com.qmuiteam.qmui.R.style.QMUI_Dialog;

    public static DialogUtils newInstance(Context context) {
        DialogUtils dialogUtils = new DialogUtils();
        dialogUtils.mContext = context;
        return dialogUtils;
    }

    private DialogUtils() {
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg != null) {
                if (msg.what == 0) {
                    if (tipDialog != null) {
                        tipDialog.dismiss();
                        tipDialog = null;
                    }
                }
            }
        }
    };

    public void showHUD(String label) {
        label = label == null ? "请稍后..." : label;
        hud = KProgressHUD.create(mContext)
                          .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                          .setLabel(label)
                          .setCancellable(false)
                          .setDimAmount(0.5f);
        hud.show();

    }

    /**
     * 关闭hud的显示
     */
    public void hudDismiss() {
        if (hud != null) {
            hud.dismiss();
        }
    }

    /**
     * 显示成功提醒
     *
     * @param tipWord
     */
    public void showSuccessTip(String tipWord) {
        showTip(tipWord,
                QMUITipDialog.Builder.ICON_TYPE_SUCCESS);

    }

    /**
     * 显示成功提醒
     *
     * @param tipWord
     */
    public void showFailTip(String tipWord) {
        showTip(tipWord,
                QMUITipDialog.Builder.ICON_TYPE_FAIL);
    }

    /**
     * 显示消息提醒
     *
     * @param tipWord
     */
    public void showInfoTip(String tipWord) {
        showTip(tipWord,
                QMUITipDialog.Builder.ICON_TYPE_INFO);
    }

    /**
     * 显示提醒
     *
     * @param tipWord
     * @param iconType
     */
    public void showTip(String tipWord, int iconType) {
        tipWord = Optional.ofNullable(tipWord)
                          .orElse("");
        tipDialog = new QMUITipDialog.Builder(mContext).setIconType(iconType)
                                                       .setTipWord(tipWord)
                                                       .create();
        tipDialog.show();
        mHandler.postDelayed(() -> {
                                 Message message = new Message();
                                 message.what = 0;
                                 mHandler.sendMessage(message);
                             },
                             1200);
    }

    /**
     * 显示删除对话框
     *
     * @param content
     * @param cancelBtnOnClick
     * @param sureBtnOnClick
     */
    public void showDeleteAlertDialog(String content,
                                      OnBtnClick cancelBtnOnClick,
                                      OnBtnClick sureBtnOnClick) {

        content = Optional.ofNullable(content)
                          .orElse("^_^");

        showNormalAlertDialog(content,
                              "确定",
                              "取消",
                              cancelBtnOnClick,
                              sureBtnOnClick);
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
    public void showNormalAlertDialog(String content,
                                      String sureBtnLabel,
                                      String cancelBtnLabel,
                                      OnBtnClick cancelBtnOnClick,
                                      OnBtnClick sureBtnOnClick) {

        showNormalAlertDialog(null,
                              content,
                              sureBtnLabel,
                              cancelBtnLabel,
                              cancelBtnOnClick,
                              sureBtnOnClick);

    }

    public void showAlertDialog(String title, String content, List<Operate> operates) {

        title = Optional.ofNullable(title)
                        .orElse("");

        content = Optional.ofNullable(content)
                          .orElse("");

        operates = Optional.ofNullable(operates)
                           .orElse(new ArrayList<>());

        QMUIDialog.MessageDialogBuilder messageDialogBuilder =
                new QMUIDialog.MessageDialogBuilder(mContext).setTitle(title)
                                                             .setMessage(content);

        operates.forEach(operate -> {
            messageDialogBuilder.addAction(operate.getBtnTitle(),
                                           (dialog, index) -> {
                                               Optional.ofNullable(operate.getOnClick())
                                                       .ifPresent(onBtnClick -> operate.getOnClick()
                                                                                       .onBtnClick());
                                               dialog.dismiss();
                                           });
        });
        messageDialogBuilder.create(mCurrentDialogStyle)
                            .show();
    }

    public void showNormalAlertDialog(String title,
                                      String content,
                                      String sureBtnLabel,
                                      String cancelBtnLabel,
                                      OnBtnClick cancelBtnOnClick,
                                      OnBtnClick sureBtnOnClick) {

        sureBtnLabel = Optional.ofNullable(sureBtnLabel)
                               .orElse("确定");

        cancelBtnLabel = Optional.ofNullable(cancelBtnLabel)
                                 .orElse("取消");

        new QMUIDialog.MessageDialogBuilder(mContext).setTitle(title)
                                                     .setMessage(content)
                                                     .addAction(cancelBtnLabel,
                                                                new QMUIDialogAction.ActionListener() {
                                                                    @Override
                                                                    public void onClick(QMUIDialog dialog,
                                                                                        int index) {
                                                                        Optional.ofNullable(cancelBtnOnClick)
                                                                                .ifPresent(onBtnClick -> {
                                                                                    cancelBtnOnClick.onBtnClick();
                                                                                });
                                                                        dialog.dismiss();
                                                                    }
                                                                })
                                                     .addAction(0,
                                                                sureBtnLabel,
                                                                QMUIDialogAction.ACTION_PROP_NEGATIVE,
                                                                new QMUIDialogAction.ActionListener() {
                                                                    @Override
                                                                    public void onClick(QMUIDialog dialog,
                                                                                        int index) {
                                                                        Optional.ofNullable(sureBtnOnClick)
                                                                                .ifPresent(onBtnClick -> {
                                                                                    sureBtnOnClick.onBtnClick();
                                                                                });
                                                                        dialog.dismiss();
                                                                    }
                                                                })
                                                     .create(mCurrentDialogStyle)
                                                     .show();

    }

    public void showCustomHUD(View customView) {

        hud = KProgressHUD.create(mContext)
                          .setCustomView(customView)
                          .setCancellable(false)
                          .setDimAmount(0.5f);
        hud.show();

    }

    /**
     * 获取一个自定义的Dialog
     *
     * @param layoutResId
     * @param title
     * @return
     */
    public Dialog getCustomDialog(@LayoutRes int layoutResId, String title) {

        return getCustomDialog(layoutResId,
                               title,
                               null,
                               "取消",
                               null,
                               null);
    }

    public Dialog getCustomDialog(@LayoutRes int layoutResId,
                                  String title,
                                  String sureBtnLabel,
                                  String cancelBtnLabel,
                                  CustomDialogOnBtnClick sureBtnOnClick,
                                  CustomDialogOnBtnClick cancelBtnOnClick) {

        QMUIDialog.CustomDialogBuilder dialogBuilder = new QMUIDialog.CustomDialogBuilder(mContext);
        dialogBuilder.setLayout(layoutResId);
        dialogBuilder.setTitle(title);
        Optional.ofNullable(cancelBtnLabel)
                .ifPresent(label -> {
                    dialogBuilder.addAction(label,
                                            new QMUIDialogAction.ActionListener() {
                                                @Override
                                                public void onClick(QMUIDialog dialog, int index) {
                                                    if (cancelBtnOnClick == null) {
                                                        dialog.dismiss();
                                                    } else {
                                                        cancelBtnOnClick.onBtnClick(dialog);
                                                        dialog.dismiss();
                                                    }
                                                }
                                            });
                });
        Optional.ofNullable(sureBtnLabel)
                .ifPresent(label -> {
                    dialogBuilder.addAction(label,
                                            new QMUIDialogAction.ActionListener() {
                                                @Override
                                                public void onClick(QMUIDialog dialog, int index) {
                                                    if (sureBtnOnClick == null) {
                                                        dialog.dismiss();
                                                    } else {
                                                        sureBtnOnClick.onBtnClick(dialog);
                                                        dialog.dismiss();
                                                    }
                                                }
                                            });
                });
        QMUIDialog dialog = dialogBuilder.create(mCurrentDialogStyle);

        return dialog;
    }

    /**
     * 显示单选dialog
     *
     * @param title
     * @param listener
     */
    public void showSingleChoiceDialog(String title,
                                       String[] items,
                                       SingleChoiceDialogListener listener) {
        items = Optional.ofNullable(items)
                        .orElse(new String[]{});
        String[] finalItems = items;
        new QMUIDialog.CheckableDialogBuilder(mContext).setTitle(title)
                                                       .addItems(items,
                                                                 new DialogInterface.OnClickListener() {
                                                                     @Override
                                                                     public void onClick(
                                                                             DialogInterface dialog,
                                                                             int which) {
                                                                         Optional.ofNullable(listener)
                                                                                 .ifPresent(onBtnClick1 -> {
                                                                                     listener.onClickListener(finalItems[which],
                                                                                                              which);
                                                                                 });

                                                                         dialog.dismiss();
                                                                     }
                                                                 })
                                                       .addAction("取消",
                                                                  new QMUIDialogAction.ActionListener() {
                                                                      @Override
                                                                      public void onClick(QMUIDialog dialog,
                                                                                          int index) {
                                                                          dialog.dismiss();
                                                                      }
                                                                  })
                                                       .create(mCurrentDialogStyle)
                                                       .show();
    }

    public interface OnBtnClick {
        void onBtnClick();
    }

    public interface CustomDialogOnBtnClick {
        void onBtnClick(QMUIDialog dialog);
    }

    public interface SingleChoiceDialogListener {
        void onClickListener(String content, int which);
    }

    public static class Operate {

        public static Operate getInstance(String title, OnBtnClick onClick) {
            Operate operate = new Operate();
            operate.btnTitle = title;
            operate.onClick = onClick;
            return operate;
        }

        private String btnTitle;
        private OnBtnClick onClick;

        public String getBtnTitle() {
            return btnTitle;
        }

        public void setBtnTitle(String btnTitle) {
            this.btnTitle = btnTitle;
        }

        public OnBtnClick getOnClick() {
            return onClick;
        }

        public void setOnClick(OnBtnClick onClick) {
            this.onClick = onClick;
        }
    }

}
