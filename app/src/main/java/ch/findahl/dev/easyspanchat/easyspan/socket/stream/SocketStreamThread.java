package ch.findahl.dev.easyspanchat.easyspan.socket.stream;


import java.net.Socket;
import java.util.concurrent.CountDownLatch;

import ch.findahl.dev.easyspanchat.easyspan.socket.SocketMessageListener;
import ch.findahl.dev.easyspanchat.easyspan.socket.SocketThread;

/**
 * Created by jesper on 05/04/15.
 */
abstract class SocketStreamThread extends SocketThread {

    private final Socket socket;
    private final CountDownLatch doneSignal;

    SocketStreamThread(Socket socket, SocketMessageListener chatMessageListener,
                       CountDownLatch doneSignal) {
        super(chatMessageListener);
        this.socket = socket;
        this.doneSignal = doneSignal;
    }

    Socket getSocket() {
        return socket;
    }

    CountDownLatch getDoneSignal() {
        return doneSignal;
    }
}
