package main.java.Connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

class ServerConnHandler extends Thread{
    private final ServerSocket sock;
    private final Server parent;

    private int maxConns;
    private int connNumber = 0;

    private boolean running;


    ServerConnHandler(ServerSocket sock, Server parent, int maxConns) {
        this.sock = sock;
        this.parent = parent;
        this.maxConns = maxConns;
    }

    @Override
    public void run (){
        running = true;
        while(running) {
            try {
                Socket clientSock = sock.accept();
                PrintWriter output = new PrintWriter(clientSock.getOutputStream(), true);
                BufferedReader input = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));

                if(SocketCommHandler.activeComms.size() >= maxConns && maxConns > 0) {
                    output.println("Too many connections. Try again later.");
                    try {
                        clientSock.close();
                        output.close();
                        input.close();
                    } catch (Exception e) {
                        System.err.println("Error closing socket after too many requests denial.");
                    }

                    continue;
                }

                int connNum = nextNum();

                System.out.println("Connection established, connection number " + connNum);


                // t is a Thread
                SocketCommHandler t = new SocketCommHandler(clientSock, output, input, connNum, parent.msgRecv, parent.onClose);
                t.setDaemon(true);
                t.start();

                parent.addConn(t);

            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    private int nextNum(){
        this.connNumber += 1;
        return this.connNumber;
    }

    public void close() {
        running = false;
        try {
            sock.close();
        } catch (IOException e) {
            System.err.println("Error closing socket.");
        }
    }
}
