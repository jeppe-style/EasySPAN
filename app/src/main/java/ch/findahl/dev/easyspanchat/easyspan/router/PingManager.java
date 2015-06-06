package ch.findahl.dev.easyspanchat.easyspan.router;

import android.util.Log;
import android.util.Pair;

import java.lang.Boolean;
import java.lang.Long;
import java.lang.Override;
import java.lang.String;
import java.lang.System;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by jesper on 17/04/15.
 */
class PingManager {

    private static final String TAG = PingManager.class.getSimpleName();
    private static final long SEND_INTERVAL = 2000;
    private static final long CHECK_INTERVAL = 3000;


    private final boolean debug = false;

    private final ConcurrentMap<String, Pair<Long, Boolean>> pingMap;
    private final Router router;

    private final String deviceId;

    private final Timer sendTimer;
    private final Timer checkTimer;

    public PingManager(String deviceId, Router router) {

        this.deviceId = deviceId;
        this.router = router;

        pingMap = new ConcurrentHashMap<>();
        sendTimer = new Timer();
        checkTimer = new Timer();

    }

    public void start() {

        if (debug)
            Log.d(TAG, "starting");

        sendTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                sendTask();
            }
        }, SEND_INTERVAL, SEND_INTERVAL);

        checkTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                checkTask();
            }
        }, CHECK_INTERVAL, CHECK_INTERVAL);

    }

    public void stop() {

        if (debug)
            Log.d(TAG, "stopping");

        sendTimer.cancel();
        checkTimer.cancel();

    }

    private void sendTask() {

        if (debug)
            Log.d(TAG, "sending ping message");

        PingMessage message = new PingMessage(deviceId);

        router.sendPingMessage(message);

    }

    private void checkTask() {

        if (debug)
            Log.d(TAG, "checking ping map");

        Set<String> deviceIds = pingMap.keySet();

        for (String deviceId : deviceIds) {
            long interval = System.currentTimeMillis() - pingMap.get(deviceId).first;

            if (interval > 3000) {

                router.onDeviceNotAlive(deviceId);
                pingMap.remove(deviceId);

            }

        }

    }

    public void onPingMessage(PingMessage message) {

        if (debug)
            Log.d(TAG, "received ping message from " + message.getDeviceId());

        if (message.isAlive() == null) { // received ping request

            message.setAlive();
            message.setDeviceId(deviceId);

            router.sendPingMessage(message);

        } else { // received ping answer

            Pair<Long, Boolean> pingInfo = new Pair<>(System.currentTimeMillis(), message.isAlive());

            pingMap.put(message.getDeviceId(), pingInfo);

        }

    }

}
