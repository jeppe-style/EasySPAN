package ch.findahl.dev.easyspanchat.easyspan.routing;


import android.util.Log;

import java.io.IOException;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import ch.findahl.dev.easyspanchat.easyspan.EasySpanManager;
import ch.findahl.dev.easyspanchat.easyspan.DeviceInfo;
import ch.findahl.dev.easyspanchat.easyspan.socket.SocketErrorListener;

/**
 * Created by jesper on 03/04/15.
 */
public class RoutingManager {

    private static final String TAG = RoutingManager.class.getSimpleName();

    static final int PORT = 4446;
    static final String GROUP_IP = "224.0.0.4";
    static final int BUFFER_SIZE = 256;
    static final long PACKET_INTERVAL = 1000;
    private static final long TIME_OUT_INTERVAL = 10000;

    private final boolean debug = false;

    private final DeviceInfo thisDeviceInfo;
    private final ConcurrentMap<String, DeviceInfo> routerMap;
    private final ConcurrentMap<String, Long> routerMapTimeStamps;
    private final Timer checkRouterMapTimer;

    private final EasySpanManager spanManager;
    private final SocketErrorListener socketErrorListener;

    public RoutingManager(DeviceInfo thisDeviceInfo, EasySpanManager spanManager,
                          SocketErrorListener socketErrorListener) {
        this.thisDeviceInfo = thisDeviceInfo;
        this.spanManager = spanManager;
        this.socketErrorListener = socketErrorListener;

        routerMap = new ConcurrentHashMap<>();
        routerMapTimeStamps = new ConcurrentHashMap<>();
        checkRouterMapTimer = new Timer();
    }

    private RoutingMulticastService routingService;

    public void startRouting() {

        if (routingService == null) {

            routingService = new RoutingMulticastService(this, thisDeviceInfo, socketErrorListener);

            try {

                routingService.startService();
                checkRouterMapTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        checkRouterMap();
                    }
                }, PACKET_INTERVAL, TIME_OUT_INTERVAL);

            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            Log.d(TAG, "Routing already running!");
        }


    }

    public void stopRouting() {

        if (routingService != null) {

            routingService.stopService();
            checkRouterMapTimer.cancel();

        } else {
            Log.d(TAG, "Routing not running!");
        }


    }

    public void onPacketReceived(Object packetObject) {

        if(packetObject instanceof DeviceInfo) {

            DeviceInfo deviceInfo = (DeviceInfo) packetObject;
            boolean newRoute = !routerMap.containsKey(deviceInfo.getId());
            routerMap.put(deviceInfo.getId(), deviceInfo);
            routerMapTimeStamps.put(deviceInfo.getId(), System.currentTimeMillis());
            if (newRoute)
                spanManager.onRouteAdded(deviceInfo.getId(), deviceInfo);

            if (debug) {
                Log.d(TAG, "received new multicast packet: " + deviceInfo);
            }

        }

    }

    private void checkRouterMap() {

        if (debug)
            Log.d(TAG, "checking routing map");

        Set<String> deviceIds = routerMap.keySet();

        for (String deviceId : deviceIds) {
            long interval = System.currentTimeMillis() - routerMapTimeStamps.get(deviceId);

            if (interval > TIME_OUT_INTERVAL) {
                routerMap.remove(deviceId);
                spanManager.onRouteRemoved(deviceId);
            }

        }

        if (debug)
            Log.d(TAG, "RouterMap has size: " + routerMap.size());

    }

    public void deviceDisconnected(String deviceId) {

        routerMap.remove(deviceId);
        spanManager.onRouteRemoved(deviceId);

    }


}
