package ch.findahl.dev.easyspanchat.easyspan.routing;

import android.util.Log;

import java.io.IOException;
import java.lang.Object;
import java.lang.String;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

import ch.findahl.dev.easyspanchat.easyspan.DeviceInfo;
import ch.findahl.dev.easyspanchat.easyspan.socket.SocketErrorListener;
import ch.findahl.dev.easyspanchat.easyspan.wifidirect.Utilities;

/**
 * Created by jesper on 03/04/15.
 */
class RoutingMulticastService {

    private static final String TAG = RoutingMulticastService.class.getSimpleName();

    private static final int PORT = 8000;

    private MulticastSocket multicastSocket;

    private RoutingMulticastSource routingSource;
    private RoutingMulticastReceiver routingClient;

    private final DeviceInfo thisDeviceInfo;

    private final RoutingManager routingManager;
    private final SocketErrorListener socketErrorListener;

    public RoutingMulticastService(RoutingManager routingManager, DeviceInfo thisDeviceInfo,
                                   SocketErrorListener socketErrorListener) {
        this.routingManager = routingManager;
        this.thisDeviceInfo = thisDeviceInfo;
        this.socketErrorListener = socketErrorListener;
    }

    public void startService() throws IOException {

        multicastSocket = new MulticastSocket(RoutingManager.PORT);

        multicastSocket.setBroadcast(true);

        InetAddress group = InetAddress.getByName(RoutingManager.GROUP_IP);

        InetSocketAddress socketAddress = new InetSocketAddress(RoutingManager.GROUP_IP, PORT);

        NetworkInterface wifiDirectNetworkInterface = Utilities.getWifiDirectNetworkInterface();
        multicastSocket.joinGroup(socketAddress, wifiDirectNetworkInterface);
        multicastSocket.setNetworkInterface(wifiDirectNetworkInterface);

        Log.d(TAG, "multicast on: " + multicastSocket.getNetworkInterface().getDisplayName());

        routingSource = new RoutingMulticastSource();
        routingSource.initialize(multicastSocket, thisDeviceInfo, group, this, socketErrorListener);

        routingClient = new RoutingMulticastReceiver();
        routingClient.initialize(multicastSocket, thisDeviceInfo, group, this, socketErrorListener);

        routingSource.start();

        routingClient.start();

    }

    public void stopService() {

        routingSource.stopThread();

        routingClient.stopThread();

        multicastSocket.close();

    }

    public void onPacketReceived(Object object) {

        routingManager.onPacketReceived(object);

    }
}
