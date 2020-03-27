package zwc.com.cloverstudio.app.corelibs.network;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import zwc.com.cloverstudio.app.corelibs.utils.LogUtils;

/**
 * @ClassName:NetworkCallbackImpl
 * @功能：网络连接状态回调
 * @作者：wlei
 * @日期：2019-11-25-00:22
 **/
public class NetworkCallbackImpl extends ConnectivityManager.NetworkCallback {
    @Override
    public void onAvailable(Network network) {
        super.onAvailable(network);
        LogUtils.log("网络已连接");
    }

    @Override
    public void onLost(Network network) {
        super.onLost(network);
        LogUtils.log("网络已丢失");
    }

    @Override
    public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
        super.onCapabilitiesChanged(network,
                                    networkCapabilities);
        if (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                LogUtils.log("onCapabilitiesChanged: 网络类型为wifi");
                //post(NetType.WIFI);
            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                LogUtils.log("onCapabilitiesChanged: 蜂窝网络");
                //post(NetType.CMWAP);
            } else {
                LogUtils.log("onCapabilitiesChanged: 其他网络");
                //post(NetType.AUTO);
            }
        }
    }
}
