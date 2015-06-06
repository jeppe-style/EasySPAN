package ch.findahl.dev.easyspanchat.easyspan.router;

import java.io.Serializable;
import java.lang.Boolean;
import java.lang.String;

/**
 * Created by jesper on 17/04/15.
 */
class PingMessage implements Serializable {

    private Boolean alive;
    private String deviceId;

    public PingMessage(String deviceId) {
        this.alive = true;
        this.deviceId = deviceId;
    }

    public Boolean isAlive() {
        return alive;
    }

    public void setAlive() {
        this.alive = true;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
