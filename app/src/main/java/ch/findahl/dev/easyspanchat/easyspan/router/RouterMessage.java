package ch.findahl.dev.easyspanchat.easyspan.router;

import java.io.Serializable;
import java.lang.Override;
import java.lang.String;
import java.util.Map;

import ch.findahl.dev.easyspanchat.easyspan.DeviceInfo;

/**
 * Created by jesper on 28/03/15.
 */
public class RouterMessage implements Serializable {

    private Map<String, DeviceInfo> routerMap;
    private DeviceInfo deviceInfo;

    public RouterMessage(Map<String, DeviceInfo> routerMap, DeviceInfo deviceInfo) {
        this.routerMap = routerMap;
        this.deviceInfo = deviceInfo;
    }

    public Map<String, DeviceInfo> getRouterMap() {
        return routerMap;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    @Override
    public String toString() {
        return "RouterMessage{" +
                "routerMap=" + routerMap +
                ", deviceInfo=" + deviceInfo +
                '}';
    }
}
