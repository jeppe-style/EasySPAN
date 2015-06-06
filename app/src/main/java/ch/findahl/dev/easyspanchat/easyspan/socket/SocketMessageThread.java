package ch.findahl.dev.easyspanchat.easyspan.socket;

import java.lang.Object;

/**
 * Created by jesper on 17/04/15.
 */
public abstract class SocketMessageThread extends SocketThread {

    SocketMessageThread(SocketMessageListener messageListener) {
        super(messageListener);
    }

    public abstract void sendMessage(Object message);
}
