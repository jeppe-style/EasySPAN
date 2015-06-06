package ch.findahl.dev.easyspanchat.easyspan.socket;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by jesper on 16/04/15.
 */
public class SocketClient extends AbstractSocketClient {

    private final String remoteAddress;
    private final int port;

    public SocketClient(SocketMessageListener messageListener, String
            remoteAddress, int port) {
        super(messageListener);
        this.remoteAddress = remoteAddress;
        this.port = port;
    }

    private void beforeBeforeRun() {
        try {
            super.socket = new Socket(remoteAddress, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void beforeRun() {
        beforeBeforeRun();
        super.beforeRun();
    }

}
