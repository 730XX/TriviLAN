package com.example.emptyapplication.data.network;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;

public final class NetworkUtils {

    private static final String FALLBACK_IP = "127.0.0.1";

    private NetworkUtils() {
        // Utility class
    }

    public static String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
                for (NetworkInterface networkInterface : Collections.list(interfaces)) {
                    Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                    for (InetAddress address : Collections.list(addresses)) {
                        if (address instanceof Inet4Address && !address.isLoopbackAddress()) {
                            String hostAddress = address.getHostAddress();
                            if (hostAddress != null && !hostAddress.isEmpty()) {
                                return hostAddress;
                            }
                        }
                    }
                }
            }
        } catch (SocketException ignored) {
            // Return fallback IP on network interface access error
        }
        return FALLBACK_IP;
    }
}
