package ch.findahl.dev.easyspanchat.easyspan.wifidirect;

import android.content.Context;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pGroupList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import ch.findahl.dev.easyspanchat.easyspan.EasySpanActivity;
import ch.findahl.dev.easyspanchat.easyspan.EasySpanManager;

/**
 * Created by jesper on 28/03/15.
 */
public class WifiDirectManager implements WifiP2pManager.PeerListListener, WifiP2pManager.PersistentGroupInfoListener {

    private static final String TAG = WifiDirectManager.class.getSimpleName();
    private boolean removeGroups;
    private Timer discoverTimer;

    private WifiP2pDeviceList mPeers;

    private void logDebugMessage(String msg) {
        boolean debug = true;
        if (debug)
            Log.d(TAG, msg);
    }

    private final EasySpanActivity mActivity;
    private final IntentFilter intentFilter = new IntentFilter();

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;

    private WiFiDirectBroadcastReceiver mReceiver;
    private final WifiDirectConnectionListener mConnectionListener;

    private boolean isWifiP2pEnabled = false;
    private boolean mLastGroupFormed = false;
    private boolean mWifiP2pSearching = false;

    private String thisDeviceName;

    private WifiP2pDevice thisWifiP2pDevice;

    private boolean isGroupOwner = false;

    private final List<WifiP2pDevice> wifiP2pDeviceList;
    private WifiP2pDevice connectingToDevice;

    private final EasySpanManager spanManager;

    private final Random random = new Random(System.currentTimeMillis());

    public WifiDirectManager(EasySpanActivity activity, String thisDeviceName, EasySpanManager spanManager) {

        mActivity = activity;

        this.thisDeviceName = thisDeviceName;
        this.spanManager = spanManager;

        this.mConnectionListener = new WifiDirectConnectionListener(this);

        this.wifiP2pDeviceList = new ArrayList<>();

    }

    /**
     * Initializes intents and WifiP2PManager.
     * Should be called in the main activities onCreate method.
     */
    public void initialize() {

        mManager = (WifiP2pManager) mActivity.getSystemService(Context.WIFI_P2P_SERVICE);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, this);

        mChannel = mManager.initialize(mActivity, mActivity.getMainLooper(), null);

        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onFailure(int reason) {
                logDebugMessage("failed to remove current group");
            }

            @Override
            public void onSuccess() {
                logDebugMessage("removed current group");
            }
        });


        //  Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);

    }

    /**
     * Should be called in the main activites onResume method.
     */
    public void resume() {

        logDebugMessage("Resuming");

        mActivity.registerReceiver(mReceiver, intentFilter);

        if (mManager != null) {
            logDebugMessage("Stopping Peer Discovery");
            mManager.stopPeerDiscovery(mChannel, null);
        }

//        if (connectionsAvailable)
//            startService(isGroupOwner, groupOwnerAddress);

//        if (mManager != null) {
//            mManager.requestPeers(mChannel, mPeerListListener);
//        }


//        logDebugMessage("starting registration");
//        startRegistration();
//
//
//        logDebugMessage("discovering services");
//        initializeDiscoverService();
//
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//
//                logDebugMessage("discovering services 2nd time");
//                discoverService();
//            }
//        }, 10000);

    }

    /**
     * Should be called in the easy span manager onPause method.
     */
    public void pause() {

        logDebugMessage("Pausing");
        if (mManager != null) {
            mManager.stopPeerDiscovery(mChannel, null);
        }
        removeCurrentGroup();
        mActivity.unregisterReceiver(mReceiver);
        if (discoverTimer != null) discoverTimer.cancel();

    }

    public void stop() {

        logDebugMessage("Stopping");
        removeCurrentGroup();

    }

    public void destroy() {


        logDebugMessage("destroying");

        stopService();

    }


    @SuppressWarnings("SameReturnValue")
    public boolean discoverPeers() {


        if (!isWifiP2pEnabled) {
            Toast.makeText(mActivity, "Enable P2P from system settings",
                    Toast.LENGTH_SHORT).show();
            return true;
        }

        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {

                logDebugMessage("Discovery Initiated");

                final long time = random.nextInt(10000) + 10000;

                discoverTimer = new Timer();
                discoverTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        logDebugMessage("discovery timer triggered");
                        if (thisWifiP2pDevice.status != WifiP2pDevice
                                .CONNECTED) {
                            // device is not connected so we create a group
                            connectingToDevice = null;
                            logDebugMessage("device is not connected so a group should be " +
                                    "created. timer=" + time);
//                            stopPeerDiscovery();
                            createGroup();
                        }
                    }
                }, time);

            }

            @Override
            public void onFailure(int reasonCode) {
                String reason;

                switch (reasonCode) {
                    case WifiP2pManager.P2P_UNSUPPORTED:
                        reason = "p2p unsupported";
                        break;

                    case WifiP2pManager.ERROR:
                        reason = "error";
                        break;

                    case WifiP2pManager.BUSY:
                        reason = "busy";
                        break;

                    default:
                        reason = "";
                        break;
                }

                Toast.makeText(mActivity, "Discovery Failed : " + reason,
                        Toast.LENGTH_SHORT).show();

                logDebugMessage("Discovery Failed: " + reason);
            }
        });

        return true;

    }

    @SuppressWarnings("SameReturnValue")
    public boolean stopPeerDiscovery() {

        if (!isWifiP2pEnabled) {
            Toast.makeText(mActivity, "Enable P2P from system settings",
                    Toast.LENGTH_SHORT).show();
            return true;
        }

        mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

                logDebugMessage("Stop peer discovery success");
            }

            @Override
            public void onFailure(int i) {

                logDebugMessage("Stop peer discovery failed");
            }
        });

        mManager.cancelConnect(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

                logDebugMessage("Cancel connect successful");
            }

            @Override
            public void onFailure(int i) {

                logDebugMessage("Cancel connect failed");
            }
        });

        return true;
    }

    /**
     * Remove all peers and clear all fields. This is called on
     * BroadcastReceiver receiving a state change event.
     */
    private void resetData() {

        if (!wifiP2pDeviceList.isEmpty())
            wifiP2pDeviceList.clear();

        stopService();

    }


    /**
     *
     * @param isGroupOwner if this device is group owner
     * @param groupOwnerAddress the address of the group owner
     */
    void startService(boolean isGroupOwner, String groupOwnerAddress) {

        logDebugMessage("Starting services");

        logDebugMessage("Is GO? " + isGroupOwner);

        this.isGroupOwner = isGroupOwner;

        spanManager.startService(isGroupOwner, groupOwnerAddress, thisWifiP2pDevice.deviceAddress);

    }

    /**
     *
     */
    private void stopService() {

        logDebugMessage("Stopping service");

        if (spanManager.isServiceRunning()) {

            spanManager.stopService();
        }

    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {

        logDebugMessage("peers available");

        mPeers = wifiP2pDeviceList;

        this.wifiP2pDeviceList.clear();
        this.wifiP2pDeviceList.addAll(mPeers.getDeviceList());

        for (WifiP2pDevice device : this.wifiP2pDeviceList) {
            if (device.isGroupOwner()) logDebugMessage("Found GO: " + device.deviceName);
        }

        if (mWifiP2pSearching) connectToDevices();

        updateDevicesTextView();

    }

    public void handlePeersChanged() {

        // request available peers from the wifi p2p manager. This is an
        // asynchronous call and the calling activity is notified with a
        // callback on PeerListListener.onPeersAvailable()

        logDebugMessage("Peers changed");

        mManager.requestPeers(mChannel, this);

    }

    public void handleConnectionChanged(NetworkInfo networkInfo, WifiP2pInfo wifiP2pInfo) {

        logDebugMessage("Connection Changed");

        if (networkInfo.isConnected()) {
            logDebugMessage("is connected");

            // We are connected with the other device, request connection info to find group
            // owner IP
            mManager.requestConnectionInfo(mChannel, mConnectionListener);
            getGroupInfo();
            connectingToDevice = null;

        }
        else if (!mLastGroupFormed){
            logDebugMessage("is not connected");
            Toast.makeText(mActivity, "disconnected",
                    Toast.LENGTH_SHORT).show();

            // start search when we are disconnected, but not on group removed broadcast event
            startSearch();

        } else {
            Toast.makeText(mActivity, "disconnected",
                    Toast.LENGTH_SHORT).show();
            logDebugMessage("group removed broadcast");
        }


        mLastGroupFormed = wifiP2pInfo.groupFormed;
    }

    private void startSearch() {
        if (mManager != null && !mWifiP2pSearching) {
            discoverPeers();
        }
    }

    public void handleDeviceChanged(WifiP2pDevice device) {
        logDebugMessage("DeviceChange");
        logDebugMessage("DeviceName?" + device.deviceName);
        thisWifiP2pDevice = device;
        boolean isP2pConnected = device.status == WifiP2pDevice.CONNECTED || device.isGroupOwner();
        logDebugMessage("Connected?" + isP2pConnected);
        logDebugMessage("GO?" + device.isGroupOwner());
        if (isP2pConnected && discoverTimer!= null) {
            discoverTimer.cancel();
            discoverTimer = null;
        }
    }

    public void handleDiscoveryChanged(int discoveryState) {


        if (discoveryState == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED) {
            mWifiP2pSearching = true;
            Toast.makeText(mActivity, "discovery started",
                    Toast.LENGTH_SHORT).show();

        } else {
            mWifiP2pSearching = false;
            if (discoverTimer != null) {
                discoverTimer.cancel();
                discoverTimer = null;
            }
            Toast.makeText(mActivity, "discovery stopped",
                    Toast.LENGTH_SHORT).show();
        }
        logDebugMessage("Discovery Changed: searching? " + mWifiP2pSearching);
    }

    public void handleP2pStateChanged(int state) {

        // UI update to indicate wifi p2p status.

        if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
            // Wifi Direct mode is enabled
            isWifiP2pEnabled = true;
            logDebugMessage("P2P state changed - ENABLED");
        } else {
            isWifiP2pEnabled = false;
            resetData();
            logDebugMessage("P2P state changed - DISABLED");
        }

    }

    /**
     *
     */
    private void connectToDevices() {
        if (thisWifiP2pDevice == null)
            return;

        logDebugMessage("connectToDevices");

        updateDevicesTextView();

        // do nothing if group owner, device already connected or currently connecting
        logDebugMessage("isGO? " + isGroupOwner);
        logDebugMessage("isConnected? " + (thisWifiP2pDevice.status == WifiP2pDevice.CONNECTED));
        logDebugMessage("connecting? " + (connectingToDevice != null));
        if (isGroupOwner || (thisWifiP2pDevice.status == WifiP2pDevice.CONNECTED) ||
                connectingToDevice != null) return;

        // find group owner
        WifiP2pDevice groupOwner = getGroupOwner();

        logDebugMessage("Found GO? " + (groupOwner != null));

        if (groupOwner != null) {
            logDebugMessage("connecting to GO: " + groupOwner.deviceName);
            connectToDevice(groupOwner);
        }

    }

    private WifiP2pDevice getGroupOwner() {

        for (WifiP2pDevice device : wifiP2pDeviceList) {
            if (device.isGroupOwner())
                return device;
        }

        return null;
    }

    public boolean removeCurrentGroup() {
        if (mManager == null)
            return false;

        mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onFailure(int reason) {
                logDebugMessage("could not stop peer discovery to disconnect");
            }

            @Override
            public void onSuccess() {
                mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        isGroupOwner = false;
                        logDebugMessage("Removed current group");
                    }

                    @Override
                    public void onFailure(int reason) {
                        logDebugMessage("Could not remove current group");
                    }
                });
            }
        });


        return true;

    }

    private void removeAllGroups() {

        if (mManager == null) return;

        removeGroups = true;

        mManager.requestPersistentGroupInfo(mChannel, this);

    }

    /**
     * The requested stored p2p group info list is available
     *
     * @param groups Wi-Fi p2p group info list
     */
    @Override
    public void onPersistentGroupInfoAvailable(WifiP2pGroupList groups) {

        if (!removeGroups) return;

        for (final WifiP2pGroup group : groups.getGroupList()) {

            mManager.deletePersistentGroup(mChannel, group.getNetworkId(), new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    logDebugMessage("Removing group: " + group.getNetworkId());
                }

                @Override
                public void onFailure(int reason) {
                    logDebugMessage("Failed to remove group: " + group.getNetworkId());
                }
            });

        }

        removeGroups = false;
    }

    @SuppressWarnings("SameReturnValue")
    public boolean createGroup() {
        logDebugMessage("Creating group");

        if (!mWifiP2pSearching) {
            // discover peer first so that other devices can see new group
            mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                    mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            logDebugMessage("created group");
                            Toast.makeText(mActivity, "group created",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(int i) {
                            logDebugMessage("failed to create group, " + parseActionListenerError(i));
                            Toast.makeText(mActivity, "Could not create group. Turn Wi-Fi OFF and ON.",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onFailure(int reason) {
                    // TODO - handle failure
                }

            });
        } else {

            mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    logDebugMessage("created group");
                    Toast.makeText(mActivity, "group created",
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int i) {
                    logDebugMessage("failed to create group, " + parseActionListenerError(i));
                    Toast.makeText(mActivity, "Could not create group. Turn Wi-Fi OFF and ON.",
                            Toast.LENGTH_LONG).show();
                }
            });
        }


        return true;
    }

    private void getGroupInfo() {
        logDebugMessage("getting group info");
        mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {
                logDebugMessage("onGroupInfoAvailable");
                if (wifiP2pGroup == null) logDebugMessage("null");
                else {
                    logDebugMessage("name: " + wifiP2pGroup.getNetworkName());
                    logDebugMessage("pass: " + wifiP2pGroup.getPassphrase());
                }
            }
        });
    }

    private void connectToDevice(final WifiP2pDevice device) {

        connectingToDevice = device;

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
//        config.wps.setup = WpsInfo.PBC;
//        config.groupOwnerIntent = random.nextInt(10);

        logDebugMessage("connecting to " + device.deviceName + " with GO intent: " +
                config.groupOwnerIntent);

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                logDebugMessage("Connection initiated to " + device.deviceName);
            }

            @Override
            public void onFailure(int i) {

                logDebugMessage("Connection initiation failure to " + device.deviceName + ", " +
                        "error=" + parseActionListenerError(i));

                if (i == WifiP2pManager.BUSY) {

                    logDebugMessage("Trying to connect again");
                    connectingToDevice = null;

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            connectToDevices();
                        }
                    }, 2000);

                }

            }
        });
    }

    private void updateDevicesTextView() {

        spanManager.updateDevicesTextView();

    }

    private void startRegistration() {

        Map<String, String> record = new HashMap<>();
        record.put("service", "easy_span_chat");
        record.put("deviceName", thisDeviceName);
        record.put("isGO", String.valueOf(isGroupOwner));

        WifiP2pDnsSdServiceInfo serviceInfo = WifiP2pDnsSdServiceInfo.newInstance("_easySpan",
                "_presence._tcp", record);


        mManager.addLocalService(mChannel, serviceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

                logDebugMessage("service added");
            }

            @Override
            public void onFailure(int i) {

                logDebugMessage("service could not be added: " + i);
            }
        });


    }

    private void initializeDiscoverService() {

        WifiP2pManager.DnsSdTxtRecordListener txtRecordListener = new WifiP2pManager.DnsSdTxtRecordListener() {
            @Override
            public void onDnsSdTxtRecordAvailable(String fullDomain, Map record,
                                                  WifiP2pDevice wifiP2pDevice) {

                logDebugMessage("DnsSdTxtRecord found: " + record.toString() + " " + wifiP2pDevice.deviceAddress);

            }
        };

        WifiP2pManager.DnsSdServiceResponseListener serviceResponseListener = new WifiP2pManager.DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType,
                                                WifiP2pDevice resourceType) {

                logDebugMessage("onBonjourServiceAvailable " + instanceName);

            }
        };

        mManager.setDnsSdResponseListeners(mChannel, serviceResponseListener, txtRecordListener);

        WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        mManager.addServiceRequest(mChannel, serviceRequest, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                logDebugMessage("service request added");
            }

            @Override
            public void onFailure(int i) {
                logDebugMessage("service request could not be added: " + i);
            }
        });

        discoverService();

    }

    private void discoverService() {

        mManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                logDebugMessage("discovering services registered");
            }

            @Override
            public void onFailure(int i) {
                logDebugMessage("discovering services could not be registered: " + i);
            }
        });

    }

    public void setThisDeviceName(String thisDeviceName) {
        this.thisDeviceName = thisDeviceName;
    }

    public List<WifiP2pDevice> getWifiP2pDeviceList() {
        return wifiP2pDeviceList;
    }

    private String parseActionListenerError(int i) {
        String error = "";
        switch (i) {
            case 0 : error = "ERROR"; break;
            case 1 : error = "P2P_UNSUPPORTED"; break;
            case 2 : error = "BUSY"; break;
        }

        return error;
    }


}
