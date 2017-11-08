package stark.a.is.zhang.wifitest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

public class WiFiReceiver extends BroadcastReceiver {

    private static final String TAG = "WiFiReceiver";
    private ICallBack mCallBack;

    public WiFiReceiver() {
    }

    public void setCallBack(ICallBack callBack) {
        mCallBack = callBack;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        String action = intent.getAction();
        Toast.makeText(context, "" + action, Toast.LENGTH_SHORT).show();
        Log.e(TAG, "onReceive: " + action);
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            Toast.makeText(context, "打开WebView", Toast.LENGTH_SHORT).show();
        }

        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {//这个监听wifi的打开与关闭，与wifi的连接无关
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
            Log.e("WIFI状态", "wifiState" + wifiState);
            switch (wifiState) {
                case WifiManager.WIFI_STATE_DISABLED:

                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    break;
                //
            }
        }
        // 这个监听wifi的连接状态即是否连上了一个有效无线路由，
        // 当上边广播的状态是WifiManager.WIFI_STATE_DISABLING，和WIFI_STATE_DISABLED的时候，
        // 根本不会接到这个广播。
        // 在上边广播接到广播是WifiManager.WIFI_STATE_ENABLED状态的同时也会接到这个广播，当然刚打开wifi肯定还没有连接到有效的无线
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (null != parcelableExtra) {
                NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                NetworkInfo.State state = networkInfo.getState();
                boolean isConnected = state == NetworkInfo.State.CONNECTED;//当然，这边可以更精确的确定状态
                Log.e(this.getClass().getSimpleName(), "isConnected" + isConnected);
                if (isConnected) {

                } else {

                }
            }
        }
        //这个监听网络连接的设置，包括wifi和移动数据的打开和关闭。.
        //最好用的还是这个监听。wifi如果打开，关闭，以及连接上可用的连接都会接到监听。见log
        // 这个广播的最大弊端是比上边两个广播的反应要慢，如果只是要监听wifi，我觉得还是用上边两个配合比较合适
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (info != null) {
                Log.e("CONNECTIVITY_ACTION", "info.getTypeName()" + info.getTypeName());
                Log.e("CONNECTIVITY_ACTION", "getSubtypeName()" + info.getSubtypeName());
                Log.e("CONNECTIVITY_ACTION", "getState()" + info.getState());
                Log.e("CONNECTIVITY_ACTION",
                        "getDetailedState()" + info.getDetailedState().name());
                Log.e("CONNECTIVITY_ACTION", "getDetailedState()" + info.getExtraInfo());
                Log.e("CONNECTIVITY_ACTION", "getType()" + info.getType());
                if (mCallBack != null) {
                    mCallBack.onOk();
                    abortBroadcast();
                }

            }
        }

        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {// 这个监听wifi的连接状态
            Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (null != parcelableExtra) {
                NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                NetworkInfo.State state = networkInfo.getState();
                if (state == NetworkInfo.State.CONNECTED) {
                    Toast.makeText(context, "网络连接", Toast.LENGTH_SHORT).show();

                }


                /**else if(state==State.DISCONNECTED){
                 showWifiDisconnected(context);
                 }*///昨天写的这个方法，在坐地铁的时候发现，如果地铁上有无效的wifi站点，手机会自动连接，但是连接失败后还是会接到广播，所以不能用了
            }
        }
        //这个监听网络连接的设置，包括wifi和移动数据 的打开和关闭
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (info != null) {
                if (NetworkInfo.State.CONNECTED == info.getState()) {
                    Intent pushIntent = new Intent();
//                    pushIntent.setClass(context, NotificationService.class);
                    Toast.makeText(context, "开启服务", Toast.LENGTH_SHORT).show();

                } else if (info.getType() == 1) {
                    if (NetworkInfo.State.DISCONNECTING == info.getState())
                        Toast.makeText(context, "网络连接失败", Toast.LENGTH_SHORT).show();

                }
            }
        }
    }
}
