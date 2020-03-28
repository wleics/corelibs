package zwc.com.cloverstudio.app.corelibs.utils;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.tbruyelle.rxpermissions2.RxPermissions;

/**
 * 权限验证工具
 */
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
