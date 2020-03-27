package zwc.com.cloverstudio.app.corelibs.utils;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.tbruyelle.rxpermissions2.RxPermissions;

/**
 * @ClassName:PermissionsTool
 * @功能：
 * @作者：wlei
 * @日期：2020/3/27-6:19 PM
 **/
public class PermissionsTool {

    public static void installPermissions(FragmentActivity activity,
                                          io.reactivex.functions.Consumer<Boolean> consumer,
                                          String... permissions) {
        RxPermissions rxPermissions = new RxPermissions(activity);
        rxPermissions.request(permissions)
                     .subscribe(consumer);
    }

    public static void installPermissions(Fragment fragment,
                                          io.reactivex.functions.Consumer<Boolean> consumer,
                                          String... permissions) {
        RxPermissions rxPermissions = new RxPermissions(fragment);
        rxPermissions.request(permissions)
                     .subscribe(consumer);
    }
}
