package Connection.Discovery;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class DiscoveryClient {
    private static final int msTimeout = 5000;
    private static final int msWaittime = 7500;
    private static int port = 1998;
    private static String sendMessage = "sbsloc";

    public static List<InetAddress> getServers() {
        // Create the socket
        DatagramSocket sock;

        try {
            sock = new DatagramSocket();

            sock.setSoTimeout(msTimeout);
            sock.setBroadcast(true);
        } catch (SocketException e) {
            System.err.println("Error creating socket. ");
            e.printStackTrace();

            return null;
        }

        // Form the message and packet
        byte[] buff = sendMessage.getBytes();
        DatagramPacket pack;

        try {
            // Loop through all network interfaces and broadcast the message to each of the broadcast addresses
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while(interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback()) // Skip if it is this device
                    continue;
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast == null)
                        continue;

                    // Form the packet

                    pack = new DatagramPacket(buff, buff.length, broadcast, port);

                    // Send the message
                    try {
                        System.out.println("Message: " + sendMessage + " is being sent");
                        sock.send(pack);
                    } catch (IOException e) {
                        System.err.println("IOException when sending packet.");
                        e.printStackTrace();

                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            return null;
        }

        // Form the reception packet obj
        byte[] recvData = new byte[8];
        DatagramPacket recvPack = new DatagramPacket(recvData, recvData.length);

        // Wait/Loop for the wait period and collate all addresses which sent an acknowledgement
        long endTime = System.currentTimeMillis() + msWaittime;

        List<InetAddress> addrs = new ArrayList<>();

        while(System.currentTimeMillis() < endTime) {
            try {
                sock.receive(recvPack);

                addrs.add(recvPack.getAddress());
            } catch (IOException ignored) {}
        }

        sock.close();

        return addrs;
    }


    public static int getMsTimeout() {
        return msTimeout;
    }

    public static int getMsWaittime() {
        return msWaittime;
    }

    public static int getPort() {
        return port;
    }

    public static void setPort(int port) throws PortOutOfRangeException {
        if (0 <= port && port <= 65535) {
            DiscoveryClient.port = port;
        } else {
            throw new PortOutOfRangeException();
        }
    }

    public static String getSendMessage() {
        return sendMessage;
    }

    public static void setSendMessage(String sendMessage) {
        DiscoveryClient.sendMessage = sendMessage;
    }
}
