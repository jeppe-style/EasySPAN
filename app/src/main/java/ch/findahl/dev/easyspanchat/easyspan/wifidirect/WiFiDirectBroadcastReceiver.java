package ch.findahl.dev.easyspanchat.easyspan.wifidirect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.lang.Override;
import java.lang.String;

/**
 * Created by jesper on 04/03/15.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = WiFiDirectBroadcastReceiver.class.getSimpleName();

    private void logDebugMessage(String msg) {
        boolean debug = true;
        if (debug)
            Log.d(TAG, msg);
    }

    private final WifiP2pManager mManager;
    private final WifiDirectManager mWifiDirectManager;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager,
                                       WifiDirectManager wifiDirectManager) {

        super();
        this.mManager = manager;
        this.mWifiDirectManager = wifiDirectManager;

    }


    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            logDebugMessage("State changed");
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            mWifiDirectManager.handleP2pStateChanged(state);
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            logDebugMessage("Peers changed");
            mWifiDirectManager.handlePeersChanged();
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            logDebugMessage("Connection changed");
            // Respond to new connection or disconnections
            if (mManager == null) return;

            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            WifiP2pInfo wifiP2pInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
            mWifiDirectManager.handleConnectionChanged(networkInfo, wifiP2pInfo);

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            logDebugMessage("This device changed");
            // Respond to this device's wifi state changing
            WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            mWifiDirectManager.handleDeviceChanged(device);

        } else if (WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)) {

            int discoveryState = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE,
                    WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED);
            logDebugMessage("Discovery Changed");
            mWifiDirectManager.handleDiscoveryChanged(discoveryState);

        }

    }
}
