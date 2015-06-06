package ch.findahl.dev.easyspanchat.easyspan.routing;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

/**
 * Created by jesper on 03/04/15.
 */
public class RoutingMulticastReceiver extends RoutingMulticastThread {

    @Override
    public void doWork() {

        MulticastSocket socket = getSocket();

        byte[] buf = new byte[RoutingManager.BUFFER_SIZE];

        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        try {

            socket.receive(packet);

            Object objectReceived = byteArrayToObject(packet.getData());

            getRoutingMulticastService().onPacketReceived(objectReceived);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Object byteArrayToObject(byte[] bytes) {

        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

        try {

            ObjectInput objectInput = new ObjectInputStream(inputStream);
            Object object = objectInput.readObject();

            objectInput.close();
            inputStream.close();

            return object;

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}
