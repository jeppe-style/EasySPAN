package ch.findahl.dev.easyspanchat.easyspan;

import java.io.Serializable;
import java.lang.Override;
import java.lang.String;

/**
 * Created by jesper on 04/04/15.
 */
public class DeviceInfo implements Serializable {

    private String id;
    private String name;
    private String description;

    private String ip;

    private int unicastPort;
    private boolean isBroadcastServer;

    public DeviceInfo(String id, String name, String description, String ip, boolean isBroadcastServer) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.ip = ip;
        this.unicastPort = 0;
        this.isBroadcastServer = isBroadcastServer;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public int getUnicastPort() {
        return unicastPort;
    }

    public void setUnicastPort(int unicastPort) {
        this.unicastPort = unicastPort;
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", ip='" + ip + '\'' +
                ", unicastPort=" + unicastPort +
                ", isBroadcastServer=" + isBroadcastServer +
                '}';
    }
}
