package ch.findahl.dev.easyspanchat.easyspan.messaging;

import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import ch.findahl.dev.easyspanchat.ChatMessage;
import ch.findahl.dev.easyspanchat.easyspan.EasySpanManager;
import ch.findahl.dev.easyspanchat.easyspan.socket.SocketClient;
import ch.findahl.dev.easyspanchat.easyspan.socket.SocketMessageListener;
import ch.findahl.dev.easyspanchat.easyspan.socket.SocketMessageThread;
import ch.findahl.dev.easyspanchat.easyspan.socket.SocketServer;

/**
 * Created by jesper on 28/03/15.
 */
public class MessageManager implements SocketMessageListener {

    private static final String TAG = MessageManager.class.getSimpleName();

    private void logDebugMessage(String msg) {
        boolean debug = true;
        if (debug)
            Log.d(TAG, msg);
    }

    private static final int PUBLIC_PORT = 8488;
    public static final String BROADCAST_MESSAGE_ID = "broadcast";

    private final boolean isBroadcastServer;
    private boolean serviceStarted = false;
    private SocketMessageThread broadcastMessageThread;

    private final String groupOwnerAddress;

    private SocketServer unicastServer;
    private final int unicastPort;

    private final EasySpanManager spanManager;

    private final ConcurrentHashMap<String, SocketClient> unicastClientMap;


        public MessageManager(EasySpanManager spanManager, boolean isBroadcastServer,
        String groupOwnerAddress, int unicastPort) {

            this.spanManager = spanManager;
            this.isBroadcastServer = isBroadcastServer;
            this.groupOwnerAddress = groupOwnerAddress;

            this.unicastPort = unicastPort;

            unicastClientMap = new ConcurrentHashMap<>();

        }

    public synchronized void startMessageService() {

        logDebugMessage("Starting chat service");

        if (broadcastMessageThread == null) {

            if (isBroadcastServer) {
                broadcastMessageThread = new SocketServer(this, PUBLIC_PORT);
                broadcastMessageThread.start();
            } else {
                broadcastMessageThread = new SocketClient(this, groupOwnerAddress, PUBLIC_PORT);
                broadcastMessageThread.start();
            }

        } else {
            logDebugMessage("broadcast chat service already started");
        }

        if (unicastServer == null) {
            unicastServer = new SocketServer(this, unicastPort);
            unicastServer.start();
        } else {
            logDebugMessage("unicast chat server already started");
        }

        serviceStarted = true;

        logDebugMessage("Chat service started");

    }

    public synchronized void stopMessageService() {

        logDebugMessage("Stopping chat service");

        if (broadcastMessageThread != null) {

            if (broadcastMessageThread.isAlive()) {
                broadcastMessageThread.stopWork();
                try {
                    broadcastMessageThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            broadcastMessageThread = null;

            logDebugMessage("BroadcastMessageThread stopped");
        }
        else {
            Log.e(TAG, "no broadcast service started");
        }

        if (unicastServer != null) {

            if (unicastServer.isAlive()) {
                unicastServer.stopWork();
                try {
                    unicastServer.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            unicastServer = null;
            logDebugMessage("UnicastMessageThread stopped");
        } else {
            Log.e(TAG, "no unicast server started");
        }

        serviceStarted = false;

        logDebugMessage("Chat service stopped");

    }

    public synchronized void sendMessage(final EasySpanMessage message, String toIP, int unicastPort) {

        logDebugMessage("sending msg: " + message);

        if (message.isBroadcastMessage() && broadcastMessageThread != null) {

            broadcastMessageThread.sendMessage(message);

            if (isBroadcastServer) {

                spanManager.onMessageReceived(message, BROADCAST_MESSAGE_ID);
            }

        } else if (!message.isBroadcastMessage() && unicastServer != null) {

            logDebugMessage("not broadcast message, to:" + message.getToId());

            if (unicastServer.hasConnectionTo(toIP)) {
                unicastServer.sendUnicastMessage(message, toIP);
                spanManager.onMessageReceived(message, message.getToId());
            }
            else if (unicastClientMap.containsKey(toIP)) {

                SocketClient client = unicastClientMap.get(toIP);
                client.sendMessage(message);
                spanManager.onMessageReceived(message, message.getToId());

            } else {

                final SocketClient client = new SocketClient(this, toIP, unicastPort);
                client.start();
                unicastClientMap.put(toIP, client);
                spanManager.onMessageReceived(message, message.getToId());
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        client.sendMessage(message);
                    }
                }, 4000);

            }

        } else {
            logDebugMessage("no message server started. broadcast? " + message.isBroadcastMessage
                    ());
        }


    }

    @Override
    public void onMessage(Object message) {


        if (message instanceof EasySpanMessage) {

            EasySpanMessage easySpanMessage = (EasySpanMessage) message;

            if (isBroadcastServer && easySpanMessage.isBroadcastMessage()) {
                broadcastMessageThread.sendMessage(message);
            }

            String messageID = easySpanMessage.isBroadcastMessage() ? BROADCAST_MESSAGE_ID : (
                    (ChatMessage) message).getFromId();

            spanManager.onMessageReceived(easySpanMessage, messageID);

        }

    }

    public synchronized boolean serviceStarted() {
        return serviceStarted;
    }

}
