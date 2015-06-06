package ch.findahl.dev.easyspanchat.easyspan.wifidirect;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.lang.StringBuilder;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by jesper on 02/04/15.
 */
public class Utilities {

    private static final String TAG = Utilities.class.getSimpleName();

    private static void logDebugMessage(String msg) {
        boolean debug = true;
        if (debug)
            Log.d(TAG, msg);
    }

    public static InetAddress getWifiDirectIpv4Address() {

        List<NetworkInterface> foundInterfaces = new ArrayList<>();

        logDebugMessage("Listing IP Addresses");

        Enumeration<NetworkInterface> interfaceEnumeration = null;
        try {
            interfaceEnumeration = NetworkInterface
                    .getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }

        while (interfaceEnumeration != null && interfaceEnumeration.hasMoreElements
                ()) {

            NetworkInterface anInterface = interfaceEnumeration.nextElement();

            if (anInterface.getName().contains("p2p")) {

                foundInterfaces.add(anInterface);
            }

        }

        for (int i = 0; i < foundInterfaces.size(); i++) {

            NetworkInterface networkInterface = foundInterfaces.get(i);

            Enumeration<InetAddress> inetAdresses = networkInterface.getInetAddresses();

            for (InetAddress inetAddress : Collections.list(inetAdresses)) {

                if (inetAddress instanceof Inet4Address) { // fix for Galaxy Nexus. IPv4 is easy to use :-)
                    logDebugMessage("NetworkInterface: " + networkInterface.getName());
                    logDebugMessage("IPv4: " + inetAddress.getHostAddress());

                    return inetAddress;
                }

            }

        }

        return null;
    }

    public static NetworkInterface getWifiDirectNetworkInterface() {

        List<NetworkInterface> foundInterfaces = new ArrayList<>();

        Enumeration<NetworkInterface> interfaceEnumeration = null;
        try {
            interfaceEnumeration = NetworkInterface
                    .getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }

        while (interfaceEnumeration != null && interfaceEnumeration.hasMoreElements
                ()) {

            NetworkInterface anInterface = interfaceEnumeration.nextElement();

            if (anInterface.getName().contains("p2p")) {

                foundInterfaces.add(anInterface);
            }

        }

        for (int i = 0; i < foundInterfaces.size(); i++) {

            NetworkInterface networkInterface = foundInterfaces.get(i);

            Enumeration<InetAddress> inetAdresses = networkInterface.getInetAddresses();

            for (InetAddress inetAddress : Collections.list(inetAdresses)) {

                if (inetAddress instanceof Inet4Address) {

                    return networkInterface;
                }

            }

        }

        return null;

    }

    public static NetworkInterface getWifiDirectNetworkInterface(String mac) {

        List<NetworkInterface> foundInterfaces = new ArrayList<>();

        Enumeration<NetworkInterface> interfaceEnumeration = null;
        try {
            interfaceEnumeration = NetworkInterface
                    .getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }

        while (interfaceEnumeration != null && interfaceEnumeration.hasMoreElements
                ()) {

            NetworkInterface anInterface = interfaceEnumeration.nextElement();

            if (anInterface.getName().contains("p2p")) {

                foundInterfaces.add(anInterface);
            }

        }

        for (int i = 0; i < foundInterfaces.size(); i++) {

            NetworkInterface networkInterface = foundInterfaces.get(i);

            String interfaceMac = getMacFromNetworkInterface(networkInterface);

            assert interfaceMac != null;
            if (interfaceMac.equalsIgnoreCase(mac))
                return networkInterface;

        }

        return null;

    }

    private static String getMacFromNetworkInterface(NetworkInterface networkInterface) {

        try {
            byte[] interfaceMac = networkInterface.getHardwareAddress();

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < interfaceMac.length; i++) {
                sb.append(String.format("%02X%s", interfaceMac[i],
                        (i < interfaceMac.length - 1) ? ":" :
                                ""));
            }

            return sb.toString().toLowerCase();

        } catch (SocketException e) {
            e.printStackTrace();
        }

        return null;

    }

    public static void getArpIpFromMac(String MAC) {

        logDebugMessage("get /proc/net/arp IP from MAC");

        logDebugMessage(MAC);

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {

                logDebugMessage("full line: " + line);

//                String[] splitted = line.split(" +");
//                if (splitted != null && splitted.length >= 4) {
//                    // Basic sanity check
//                    String device = splitted[5];
//                    logDebugMessage(device);
//                    if (device.matches(".*p2p-p2p0.*")){
//                        String mac = splitted[3];
//                        logDebugMessage(mac);
//                        if (mac.toLowerCase().matches(MAC.toLowerCase())) {
//                            logDebugMessage("IP: " + splitted[0]);
//                        }
//                    }
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                assert br != null;
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
