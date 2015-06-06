package ch.findahl.dev.easyspanchat.easyspan.router;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import ch.findahl.dev.easyspanchat.easyspan.DeviceInfo;
import ch.findahl.dev.easyspanchat.easyspan.socket.SocketClient;
import ch.findahl.dev.easyspanchat.easyspan.socket.SocketMessageListener;
import ch.findahl.dev.easyspanchat.easyspan.socket.SocketMessageThread;
import ch.findahl.dev.easyspanchat.easyspan.socket.SocketServer;

/**
 * Created by jesper on 28/03/15.
 */
public class Router implements SocketMessageListener {

    private static final String TAG = Router.class.getSimpleName();

    private static final int PORT = 8844;
    private final String serverIp;

    private final boolean isServer;
    private final DeviceInfo thisDeviceInfo;
    private final PingManager pingManager;
    private SocketMessageThread messageThread;
    private final Map<String, DeviceInfo> mRouterMap;

    private Router(boolean isServer, String serverIp, DeviceInfo thisDeviceInfo) {

        this.isServer = isServer;

        this.serverIp = serverIp;

        this.thisDeviceInfo = thisDeviceInfo;

        this.pingManager = new PingManager(thisDeviceInfo.getId(), this);

        mRouterMap = new HashMap<>();

    }

    @Override
    public void onMessage(Object message) {

        if (message instanceof RouterMessage) {
            RouterMessage routerMessage = (RouterMessage) message;

            if (isServer) {
                onClientMessage(routerMessage);
            } else {
                onServerMessage(routerMessage);
            }

        } else if (message instanceof PingMessage) {

            pingManager.onPingMessage((PingMessage) message);

        }

    }

    private void onServerMessage(RouterMessage message) {

        mRouterMap.clear();
        mRouterMap.putAll(message.getRouterMap());

        String txt = "received server message: ";

        for (Map.Entry<String, DeviceInfo> entry : mRouterMap.entrySet()) {

            txt += "\n" + entry.getKey() + " " + entry.getValue().getIp();
        }

        Log.d(TAG, txt + "\n" + message);

    }

    private void onClientMessage(RouterMessage message) {

        Log.d(TAG, "received client message: " + message);

        if (message.getDeviceInfo() != null) {

            mRouterMap.put(message.getDeviceInfo().getId(), message.getDeviceInfo());

            pingManager.onPingMessage(new PingMessage(message.getDeviceInfo().getId()));

            sendRouterMap();

            Log.d(TAG, "Sending router map");
            for (Map.Entry<String, DeviceInfo> entry : mRouterMap.entrySet()) {
                Log.d(TAG, entry.getKey() + " " + entry.getValue().getIp());
            }

        } else {

            Log.d(TAG, "client message did not contain device info");

        }


    }

    void onDeviceNotAlive(String deviceId) {

        Log.d(TAG, "removing device " + deviceId);

        mRouterMap.remove(deviceId);

        sendRouterMap();

    }


    void sendPingMessage(PingMessage pingMessage) {

        if (messageThread != null) {

            messageThread.sendMessage(pingMessage);

        } else {
            Log.d(TAG, "no router service running");
        }

    }

    private void sendRouterMap() {
        RouterMessage outMessage = new RouterMessage(mRouterMap, null);

        messageThread.sendMessage(outMessage);
    }

    private void sendDeviceAliveMessage() {

        final RouterMessage routerMessage = new RouterMessage(null, thisDeviceInfo);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                messageThread.sendMessage(routerMessage);
            }
        }, 1000);

    }

    /**
     *
     */
    public void startRouterService() {

        Log.d(TAG, "Starting router service");

        if (isServer && messageThread == null) {

            messageThread = new SocketServer(this, PORT);
            messageThread.start();

            // add itself to router map
            if (isServer) {
                mRouterMap.put(thisDeviceInfo.getId(),
                        thisDeviceInfo);
            }

        } else if (messageThread == null) {

                messageThread = new SocketClient(this, serverIp, PORT);
                messageThread.start();

        } else {
            Log.d(TAG, "router service already started");
        }


        sendDeviceAliveMessage();

        pingManager.start();

    }

    /**
     *
     */
    public void stopRouterService() {

        Log.d(TAG, "Stopping router service");

        if (messageThread != null) {
            messageThread.stopWork();
        } else {
            Log.d(TAG, "no router service running");
        }

        pingManager.stop();

    }

}
