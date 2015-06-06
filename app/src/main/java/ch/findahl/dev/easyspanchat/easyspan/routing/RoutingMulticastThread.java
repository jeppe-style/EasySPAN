package ch.findahl.dev.easyspanchat.easyspan.routing;

import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.Thread;
import java.net.InetAddress;
import java.net.MulticastSocket;

import ch.findahl.dev.easyspanchat.easyspan.DeviceInfo;
import ch.findahl.dev.easyspanchat.easyspan.socket.SocketErrorListener;

/**
 * Created by jesper on 03/04/15.
 */
abstract class RoutingMulticastThread extends Thread {

    private volatile boolean isRunning = false;
    private volatile MulticastSocket socket;
    private DeviceInfo thisDeviceInfo;
    private InetAddress groupInetAddress;
    private RoutingMulticastService routingMulticastService;
    SocketErrorListener socketErrorListener;

    public void initialize(MulticastSocket socket, DeviceInfo thisDeviceInfo, InetAddress
            groupInetAddress, RoutingMulticastService routingMulticastService,
                           SocketErrorListener socketErrorListener) {
        this.socket = socket;
        this.thisDeviceInfo = thisDeviceInfo;
        this.groupInetAddress = groupInetAddress;
        this.routingMulticastService = routingMulticastService;
        this.socketErrorListener = socketErrorListener;
    }

    /**
     *
     */
    protected abstract void doWork();

    @Override
    public void run() {

        isRunning = true;

        if(socket == null || thisDeviceInfo == null)
            throw new NullPointerException("not initialized");

        while (isRunning) {

            doWork();

        }

    }

    public void stopThread() {

        isRunning = false;

    }

    MulticastSocket getSocket() {
        return socket;
    }

    DeviceInfo getThisDeviceInfo() {
        return thisDeviceInfo;
    }

    InetAddress getGroupInetAddress() {
        return groupInetAddress;
    }

    RoutingMulticastService getRoutingMulticastService() {
        return routingMulticastService;
    }
}
