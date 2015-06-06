package ch.findahl.dev.easyspanchat.easyspan.socket;

import java.net.Socket;

/**
 * Created by jesper on 06/04/15.
 */
class SocketServerAcceptClient extends AbstractSocketClient {

    public SocketServerAcceptClient(Socket socket, SocketMessageListener socketMessageListener) {
        super(socketMessageListener);
        super.socket = socket;
    }


}
