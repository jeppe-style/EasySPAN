package ch.findahl.dev.easyspanchat.easyspan.routing;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

import ch.findahl.dev.easyspanchat.easyspan.EasySpanManager;

/**
 * Created by jesper on 03/04/15.
 */
public class RoutingMulticastSource extends RoutingMulticastThread {

    private static final String TAG = RoutingMulticastSource.class.getSimpleName();

    @Override
    public void doWork() {

        MulticastSocket socket = getSocket();

        byte[] buf = objectToByteArray(getThisDeviceInfo());

        assert buf != null;
        DatagramPacket packet = new DatagramPacket(buf, buf.length, getGroupInetAddress(),
                RoutingManager.PORT);

        try {

            socket.send(packet);

            sleep(RoutingManager.PACKET_INTERVAL);

        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
            Log.d(TAG, e.toString());
            Log.d(TAG, e.getClass().getSimpleName());
            socketErrorListener.onNetworkDown(EasySpanManager.ROUTING_SOCKET_ERROR);
        }


    }

    private byte[] objectToByteArray(Object object) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {

            ObjectOutput output = new ObjectOutputStream(outputStream);
            output.writeObject(object);
            byte[] bytes = outputStream.toByteArray();

            output.close();
            outputStream.close();

            return bytes;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
