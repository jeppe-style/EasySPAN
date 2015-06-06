package ch.findahl.dev.easyspanchat;

import ch.findahl.dev.easyspanchat.easyspan.messaging.EasySpanMessage;

/**
 * Created by jesper on 04/04/15.
 */
public class ChatMessage extends EasySpanMessage {

    private String fromName;
    private String text;

    public ChatMessage(boolean broadcastMessage, String fromName, String fromId, String toId, String text) {
        super(broadcastMessage, fromId, toId);
        this.fromName = fromName;
        this.text = text;
    }

    public String getFromName() {
        return fromName;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return fromName + " -> " + text;
    }
}
