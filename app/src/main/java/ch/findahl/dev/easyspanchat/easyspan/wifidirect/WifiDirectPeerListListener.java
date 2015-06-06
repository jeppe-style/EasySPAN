package ch.findahl.dev.easyspanchat.easyspan.wifidirect;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.util.Collection;

/**
 * Created by jesper on 05/03/15.
 */
public class WifiDirectPeerListListener implements WifiP2pManager.PeerListListener {

    private static final String TAG = WifiDirectPeerListListener.class.getSimpleName();

    private void logDebugMessage(String msg) {
        boolean debug = false;
        if (debug)
            Log.d(TAG, msg);
    }


    private WifiDirectPeerListListener() {

    }


    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {

        Collection<WifiP2pDevice> wifiP2pDevices = wifiP2pDeviceList.getDeviceList();

        logDebugMessage("Peers changed. Current peers are:");

        for (WifiP2pDevice next : wifiP2pDevices) {
            logDebugMessage(next.deviceName + " status " + statusToString(next.status));

        }

//        mWifiDirectManager.setWifiP2pDeviceList(wifiP2pDevices);

    }

    public static String statusToString(int status) {

        String result = "";

        switch (status) {
            case WifiP2pDevice.AVAILABLE:
                result = "avlbl";
                break;
            case WifiP2pDevice.CONNECTED:
                result = "cnctd";
                break;
            case WifiP2pDevice.FAILED:
                result = "fld";
                break;
            case WifiP2pDevice.INVITED:
                result = "invtd";
                break;
            case WifiP2pDevice.UNAVAILABLE:
                result = "unvlbl";
                break;
        }

        return result;


    }

}
