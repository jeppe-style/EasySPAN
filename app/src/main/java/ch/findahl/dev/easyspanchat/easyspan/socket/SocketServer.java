package ch.findahl.dev.easyspanchat.easyspan.socket;

import java.io.IOException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by jesper on 16/04/15.
 */
public class SocketServer extends SocketMessageThread {

    private ServerSocket serverSocket;
    private final int port;

    private ConcurrentMap<String, SocketServerAcceptClient> accepted;

    public SocketServer(SocketMessageListener messageListener, int port) {
        super(messageListener);
        this.port = port;
    }

    private ServerSocket getServerSocket() {
        return serverSocket;
    }

    @Override
    public void beforeRun() {

        accepted = new ConcurrentHashMap<>();

        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterRun() {

        for (String socketHostIp : accepted.keySet()) {

            SocketServerAcceptClient acceptThread = accepted.get(socketHostIp);

            acceptThread.stopWork();
        }

    }

    @Override
    public void doWork() {


        Socket socket;

        try {

            socket = getServerSocket().accept();

            String socketHostIp = socket.getInetAddress().getHostAddress();

            SocketServerAcceptClient existingThread = accepted.get(socketHostIp);

            if (existingThread != null)
                existingThread.stopWork();

            SocketServerAcceptClient acceptThread = new SocketServerAcceptClient
                    (socket, getMessageListener());

            acceptThread.start();

            accepted.put(socketHostIp, acceptThread);


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void stopWork() {
        super.stopWork();
        try {
            getServerSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendMessage(Object message) {

        for (String socketHostIp : accepted.keySet()) {

            sendUnicastMessage(message, socketHostIp);

        }

    }

    public void sendUnicastMessage(Object message, String toIP) {

        SocketServerAcceptClient acceptThread = accepted.get(toIP);

        if (acceptThread.isAlive()) {

            acceptThread.sendMessage(message);

        } else {

            acceptThread.stopWork();
            accepted.remove(toIP);

        }

    }

    public boolean hasConnectionTo(String toIP) {

        SocketServerAcceptClient acceptThread = accepted.get(toIP);

        if (acceptThread == null)
            return false;

        if (acceptThread.isAlive())
            return true;

        accepted.remove(toIP);
        return false;

    }
}
