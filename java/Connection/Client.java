package main.java.Connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client{
    private int port;
    private String hostname;

    private boolean isActive = false;
    private Socket sock;
    private PrintWriter out;
    private BufferedReader in;

    private messageIn msgRecv = (message, connNum) -> {
        System.out.println("Message Received: " + message);
    };

    public SocketCommHandler commHandler;

    public Client (){
        port = 9001;
        hostname = "localhost";
    }
    public Client(int port, String hostname) {
        this.port = port;
        this.hostname = hostname;
    }

    public void start (){
        // Connect to server
        try {
            sock = new Socket(hostname, port);
            out = new PrintWriter(sock.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

            commHandler = new SocketCommHandler(sock, out, in, 0, msgRecv, null);
            commHandler.start();
        } catch (IOException exception) {
            exception.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unknown exception occurred.");
            e.printStackTrace();
        }
    }

    public void close() {
        isActive = false;
        commHandler.close();
    }
}
