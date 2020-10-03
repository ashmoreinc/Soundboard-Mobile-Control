package Connection.Discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class Server extends Thread{
    // TODO: Change the acknowledgement to return useful information about the server
    private String ackMsg;
    private int port = 1998;

    private boolean running = false;
    private boolean paused = false;

    public Server(){
        // Set defaults
        this.ackMsg = "ack";
        this.port = 1998;
    }

    public Server(String acknowledgement) {
        this.ackMsg = acknowledgement;
    }

    public Server(int port, String acknowledgement){
        this.port = port;
        this.ackMsg = acknowledgement;
    }

    @Override
    public void run() {
        DatagramSocket sock;
        try {
            sock = new DatagramSocket(port);
            sock.setSoTimeout(5000);
        } catch (SocketException e) {
            e.printStackTrace();
            return;
        }

        byte[] recvData = new byte[32];
        byte[] sendData = ackMsg.getBytes(StandardCharsets.UTF_8);

        DatagramPacket recvPack = new DatagramPacket(recvData, recvData.length);
        DatagramPacket sendPack;

        running = true;
        paused = false;

        while(running) {
            // If we are paused, sleep for five seconds and try again.
            if(paused){
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            try {
                sock.receive(recvPack);
                sendPack = new DatagramPacket(sendData, sendData.length,
                        recvPack.getAddress(), recvPack.getPort());

                String input = new String(recvPack.getData(), 0, recvPack.getLength());

                System.out.println("Input: " + input);

                // Send acknowledgement
                sock.send(sendPack);
            } catch (IOException ignored) {}
        }
    }

    public void pause(){
        paused = true;
    }

    public void unpause() {
        this.interrupt();
        paused = false;
    }

    public void togglePause(){
        paused = !paused;
    }

    public void off(){
        this.interrupt();
        running = false;
    }

    public String getAckMsg() {
        return ackMsg;
    }

    public void setAckMsg(String ackMsg) {
        this.ackMsg = ackMsg;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) throws PortOutOfRangeException {
        if (0 <= port && port <= 65535) {
            this.port = port;
        } else {
            throw new PortOutOfRangeException();
        }
    }
}
