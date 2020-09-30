package Connection.Discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class Server extends Thread{
    private String ackMsg = "ack";
    private int port = 1998;

    public Server() {}

    public Server(int port, String acknowledgement){
        this.port = port;
        this.ackMsg = acknowledgement;
    }
    @Override
    public void run() {
        DatagramSocket sock;
        try {
            sock = new DatagramSocket(port);
        } catch (SocketException e) {
            sock = null;
            e.printStackTrace();
            return;
        }

        byte[] recvData = new byte[32];
        byte[] sendData = ackMsg.getBytes(StandardCharsets.UTF_8);

        DatagramPacket recvPack = new DatagramPacket(recvData, recvData.length);
        DatagramPacket sendPack;

        while(true) {
            try {
                sock.receive(recvPack);
                sendPack = new DatagramPacket(sendData, sendData.length,
                        recvPack.getAddress(), recvPack.getPort());

                String input = new String(recvPack.getData(), 0, recvPack.getLength());

                System.out.println("Input: " + input);

                // Send acknowledgement
                sock.send(sendPack);
            } catch (IOException exception) {
                exception.printStackTrace();
            }

        }
    }

}
