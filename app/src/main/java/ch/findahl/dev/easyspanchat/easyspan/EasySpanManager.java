package ch.findahl.dev.easyspanchat.easyspan;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.util.Log;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.findahl.dev.easyspanchat.ChatMessage;
import ch.findahl.dev.easyspanchat.easyspan.messaging.EasySpanMessage;
import ch.findahl.dev.easyspanchat.easyspan.messaging.MessageManager;
import ch.findahl.dev.easyspanchat.easyspan.routing.RoutingManager;
import ch.findahl.dev.easyspanchat.easyspan.socket.SocketErrorListener;
import ch.findahl.dev.easyspanchat.easyspan.wifidirect.Utilities;
import ch.findahl.dev.easyspanchat.easyspan.wifidirect.WifiDirectManager;
import ch.findahl.dev.easyspanchat.easyspan.wifidirect.WifiDirectPeerListListener;

/**
 * Created by jesper on 17/04/15.
 */
public class EasySpanManager implements SocketErrorListener {

    private static final String TAG = EasySpanManager.class.getSimpleName();

    private static final int CHAT_SOCKET_ERROR = 0;
    public static final int ROUTING_SOCKET_ERROR = 1;

    private static final int PORTPREFIX = 8500;

    private void logDebugMessage(String msg) {
        boolean debug = true;
        if (debug) {
            Log.d(TAG, msg);
        }
    }

    private final EasySpanActivity mainActivity;
    private boolean isServer;

    private boolean isServiceRunning;

    private DeviceInfo thisDeviceInfo;
    private final Map<String, DeviceInfo> routerMap; // <deviceId=MAC, deviceInfo>

    private final WifiDirectManager wifiDirectManager;
    private String thisDeviceName;

    private MessageManager messageManager;
//    private Router router;

    private final WifiManager mWifiManager;
    private WifiManager.MulticastLock mMulticastLock;
    private static final String LOCK_TAG = "ch.findahl.dev.easyspanchat";
    private RoutingManager routingManager;

    /**
     * Constructor
     *
     * @param easySpanActivity the activity
     * @param thisDeviceName the name of the device
     */
    public EasySpanManager(EasySpanActivity easySpanActivity, String thisDeviceName) {
        this.mainActivity = easySpanActivity;

        this.thisDeviceName = thisDeviceName;

        this.routerMap = new HashMap<>();

        this.mWifiManager = (WifiManager) easySpanActivity.getApplicationContext().getSystemService
                (Context.WIFI_SERVICE);

        this.wifiDirectManager = new WifiDirectManager(easySpanActivity, thisDeviceName, this);

    }

    /**
     * Should be called in the EasySpanActivity's onCreate() method
     */
    public void initialize() {
        wifiDirectManager.initialize();
    }

    /**
     * Should be called in the EasySpanActivity's onResume() method
     */
    public void resume() {
        logDebugMessage("resuming");
        wifiDirectManager.resume();
        updateDevicesTextView();
    }

    /**
     * Should be called in the EasySpanActivity's onPause() method
     */
    public void pause() {
        logDebugMessage("pausing");

        wifiDirectManager.pause();

        if (messageManager != null) stopChatService();

        stopService();
    }

    /**
     * Should be called in the EasySpanActivity's onStop() method
     */
    public void stop() {
        wifiDirectManager.stop();
    }

    /**
     * Should be called in the EasySpanActivity's onDestory() method
     */
    public void destroy() {
        wifiDirectManager.destroy();
    }

//    public DeviceInfo getThisDeviceInfo() {
//        return thisDeviceInfo;
//    }

    /**
     * Called when the EasySpanActivity wants to send a message
     * @param message the message to send
     */
    public void onSendMessage(ChatMessage message) {

        String toIP = null;
        int unicastPort = 0;
        if (routerMap.containsKey(message.getToId())) {
            toIP = routerMap.get(message.getToId()).getIp();
            unicastPort = routerMap.get(message.getToId()).getUnicastPort();
        }

        messageManager.sendMessage(message, toIP, unicastPort);
    }

    /**
     * Called when the EasySpanActivity wants to change the name of the device
     * @param thisDeviceName the name of the device
     */
    public void setThisDeviceName(String thisDeviceName) {
        this.thisDeviceName = thisDeviceName;
        wifiDirectManager.setThisDeviceName(thisDeviceName);
        updateDevicesTextView();
    }

    /**
     * Get the name of this device
     * @return the name of the device
     */
    public String getThisDeviceName() {
        return thisDeviceName;
    }

    /**
     * Get the id of this device
     * @return the id of the device
     */
    public String getThisDeviceId() {
        return thisDeviceInfo.getId();
    }

    /**
     * Called when the EasySpanActivity wants to start to look for peers
     * @return true
     */
    public boolean discoverPeers() {
        return wifiDirectManager.discoverPeers();
    }

    /**
     * Called when the EasySpanActivity wants to stop to look for peers
     * @return true
     */
    public boolean stopPeerDiscovery() {
        return wifiDirectManager.stopPeerDiscovery();
    }

    /**
     * Called when the EasySpanActivity explicitly wants to create a group
     * @return true
     */
    public boolean createGroup() {
        return wifiDirectManager.createGroup();
    }

    /**
     * Called when the EasySpanActivity wants to remove and disconnect from the current group
     * @return true
     */
    public boolean removeCurrentGroup() {
        return wifiDirectManager.removeCurrentGroup();
    }

    public boolean isServiceRunning() {
        return isServiceRunning;
    }

    public void startService(boolean isGroupOwner, String groupOwnerAddress, String deviceAddress) {

        InetAddress deviceInetAddress = Utilities.getWifiDirectIpv4Address();

        assert deviceInetAddress != null;
        thisDeviceInfo = new DeviceInfo(deviceAddress, thisDeviceName, thisDeviceName, deviceInetAddress.getHostAddress(), isGroupOwner);
        thisDeviceInfo.setUnicastPort(assignUnicastPort(thisDeviceInfo));
        isServer = isGroupOwner;

        if (!isServiceRunning) {

            isServiceRunning = true;

            if (mWifiManager != null) {
                mMulticastLock = mWifiManager.createMulticastLock(LOCK_TAG);
                mMulticastLock.acquire();
            }


            if (routingManager == null) {
                routingManager = new RoutingManager(thisDeviceInfo, this, this);
            }
            routingManager.startRouting();
            logDebugMessage("multicast lock held? " + mMulticastLock.isHeld());

        } else {
            logDebugMessage("Service is already running");
        }

        if (messageManager == null)
            messageManager = new MessageManager(this, isServer, groupOwnerAddress, thisDeviceInfo.getUnicastPort
                    ());


    }

    @Override
    public void onNetworkDown(int error) {

        logDebugMessage("Network Down");

        if ((error == CHAT_SOCKET_ERROR || error == ROUTING_SOCKET_ERROR) && messageManager != null)
            stopChatService();

        if (error == ROUTING_SOCKET_ERROR)
            stopService();

    }

    public void stopService() {

        if (isServiceRunning) {

            logDebugMessage("stopping services");

//            router.stopRouterService();

            routingManager.stopRouting();

            mMulticastLock.release();

            isServiceRunning = false;
            messageManager = null;
            routingManager = null;

        }

    }

    public void onMessageReceived(EasySpanMessage message, String messageId) {

        logDebugMessage("onMessageReceived: " + messageId + ", " + message);

        mainActivity.onMessageReceived(message, messageId);

    }

    public void onRouteAdded(String id, DeviceInfo deviceInfo) {

        logDebugMessage("Route Added: " +deviceInfo.getName());

        this.routerMap.put(id, deviceInfo);
        onRoutesAvailable();
        updateDevicesTextView();

        if (!id.equals(thisDeviceInfo.getId()))
            mainActivity.onDeviceAdded(deviceInfo.getName(), deviceInfo.getId());

    }

    public void onRouteRemoved(String id) {

        DeviceInfo removedRoute = this.routerMap.remove(id);
        onRoutesAvailable();
        updateDevicesTextView();

        if (!id.equals(thisDeviceInfo.getId()))
            mainActivity.onDeviceRemoved(removedRoute.getName());

    }

    public void updateDevicesTextView() {

        List<WifiP2pDevice> wifiP2pDeviceList = wifiDirectManager.getWifiP2pDeviceList();
        String text = "";

        text += thisDeviceName + "(Me";
        if (isServer && isServiceRunning) text += ",GO";
        text += ") ";

        for (WifiP2pDevice next : wifiP2pDeviceList) {

            DeviceInfo nextDeviceInfo = routerMap.get(next.deviceAddress);

            String deviceName = (nextDeviceInfo == null) ? next.deviceName : nextDeviceInfo
                    .getName();

            text += deviceName + "(" + WifiDirectPeerListListener.statusToString(next
                    .status);
            if (next.isGroupOwner()) text += ",GO";
            text += ") ";

        }

        mainActivity.updateDevicesTextView(text);
    }

    private synchronized void startChatService() {


        logDebugMessage("starting chat service");

        messageManager.startMessageService();

        mainActivity.messageServiceAvailable(true);

    }

    private synchronized void stopChatService() {

        logDebugMessage("stopping chat service");

        mainActivity.messageServiceAvailable(false);

        messageManager.stopMessageService();

    }

    private synchronized void onRoutesAvailable() {

        logDebugMessage("routes available: routerMap has size " + routerMap.size());

        int numRoutes = this.routerMap.size();

        final boolean startChat = isServer ? numRoutes >= 1 : numRoutes > 1;

        logDebugMessage("start chat? " + startChat + ", chatManager.serviceStarted? " +
                messageManager.serviceStarted());

        if (startChat && !messageManager.serviceStarted())
            startChatService();
        else if (!startChat && messageManager.serviceStarted())
            stopChatService();

    }

    private int assignUnicastPort(DeviceInfo deviceInfo) {

        String ip = deviceInfo.getIp();
        int extension = Integer.parseInt(ip.substring(ip.lastIndexOf(".") + 1));

        return PORTPREFIX + extension;

    }

}
