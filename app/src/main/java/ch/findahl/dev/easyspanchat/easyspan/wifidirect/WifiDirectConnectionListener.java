package ch.findahl.dev.easyspanchat.easyspan.wifidirect;

import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.lang.Override;
import java.lang.String;

/**
 * Created by jesper on 05/03/15.
 */
class WifiDirectConnectionListener implements WifiP2pManager.ConnectionInfoListener {

    private static final String TAG = WifiDirectConnectionListener.class.getSimpleName();

    private void logDebugMessage(String msg) {
        boolean debug = false;
        if (debug)
            Log.d(TAG, msg);
    }

    private final WifiDirectManager mWifiDirectManager;

    public WifiDirectConnectionListener(WifiDirectManager wifiDirectManager) {
        mWifiDirectManager = wifiDirectManager;
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {


        if (info.groupFormed) {

            mWifiDirectManager.startService(info.isGroupOwner,
                    info.groupOwnerAddress.getHostAddress());

            // InetAddress from WifiP2pInfo struct.
            String groupOwnerAddress = info.groupOwnerAddress.getHostAddress();

            logDebugMessage("Connection info available. Group formed?" + info.groupFormed + " Group " +
                    "owner " +
                    "address: " +
                    "" +
                    groupOwnerAddress);

        } else {
            logDebugMessage("group not formed");
        }

    }
}
