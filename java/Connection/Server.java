package Connection;

import Connection.Discovery.DiscoveryServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


public class Server {
    private ReentrantLock connHandlerLock = new ReentrantLock();
    public List<SocketCommHandler> connectionHandlers = new ArrayList<>();

    private ServerConnHandler connHandler;
    private DiscoveryServer discoveryServer;

    private final int port;
    private int maxConns;
    public boolean isActive = false;

    public messageIn msgRecv = (message, connNum) -> System.out.println("Message received (" + connNum + "): " + message);

    public clientClosed onClose = this::removeConn;

    public Server (){
        port = 9001;
        maxConns = 1;
    }
    public Server (int port, int maxConns) {
        this.port = port;
        this.maxConns = maxConns;
    }

    public boolean start () {
        System.out.println("Initialising Server.");

        isActive = true;

        // Start the discovery server
        discoveryServer = new DiscoveryServer("sbs|sbs-port:" + this.port);
        discoveryServer.start();

        try {
            // Create the server socket
            ServerSocket serverSock = new ServerSocket(port);

            connHandler = new ServerConnHandler(serverSock, this, maxConns);
            connHandler.start();

            return true;
        } catch (IOException e){
            System.err.println("Exception caught when trying to listen to port " + port + " or listening for a connection.");
            System.err.println(e.getMessage());
            close();
            isActive = false;
            return false;
        }
    }

    // Manage the store connections
    public void addConn(SocketCommHandler conn) {
        connHandlerLock.lock();
        try {
            connectionHandlers.add(conn);
        } finally {
            connHandlerLock.unlock();
        }
    }

    public void removeConn(SocketCommHandler conn) {
        connHandlerLock.lock();
        try {
            connectionHandlers.remove(conn);
        } finally {
            connHandlerLock.unlock();
        }
    }

    // Manage the connections/overall socket
    public void close(){
        discoveryServer.close();
        connHandler.close();
    }

    public void closeConnectionIdx(int index) {
        connHandlerLock.lock();
        try {
            if (connectionHandlers.size() <= index || index < 0) {
                throw new ArrayIndexOutOfBoundsException();
            } else {
                connectionHandlers.get(index).close();
                connectionHandlers.remove(index);
            }
        } finally {
            connHandlerLock.unlock();
        }
    }

    public void closeConnection(int connNum) {
        connHandlerLock.lock();
        try {
            List<SocketCommHandler> connsToClose = new ArrayList<>();
            connectionHandlers.forEach(conn -> {
                if (conn.getConnNum() == connNum) {
                    conn.close(); // Close the connection and add it to the list to be removed after

                    connsToClose.add(conn);
                }
            });

            connsToClose.forEach(conn -> connectionHandlers.remove(conn));
        } finally {
            connHandlerLock.unlock();
        }
    }

    public void closeAllConnections() {
        connHandlerLock.lock();
        try {
            for(int i=0; i < connectionHandlers.size(); i++){
                connectionHandlers.get(i).close();
            }

            connectionHandlers = new ArrayList<>();
        } finally {
            connHandlerLock.unlock();
        }
    }

    public List<Integer> getConnectionNumbers(){
        List<Integer> connNums = new ArrayList<>();

        connHandlerLock.lock();
        try {
            connectionHandlers.forEach(conn -> connNums.add(conn.getConnNum()));
        } finally {
            connHandlerLock.unlock();
        }
        return connNums;
    }
}
