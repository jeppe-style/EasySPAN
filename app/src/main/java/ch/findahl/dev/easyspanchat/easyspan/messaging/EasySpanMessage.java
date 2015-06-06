package ch.findahl.dev.easyspanchat.easyspan.messaging;

import java.io.Serializable;

/**
 * Created by jesper on 01/06/15.
 */
public abstract class EasySpanMessage implements Serializable {

    private String fromId;
    private String toId;
    private boolean broadcastMessage;

    protected EasySpanMessage(boolean broadcastMessage, String fromId, String toId) {
        this.broadcastMessage = broadcastMessage;
        this.fromId = fromId;
        this.toId = toId;
    }

    boolean isBroadcastMessage(){
        return broadcastMessage;
    }

    public String getFromId() {
        return fromId;
    }

    public String getToId() {
        return toId;
    }

}
