package Connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;


public class Server {

    public List<SocketCommHandler> connectionHandlers = new ArrayList<>();

    private ServerConnHandler connHandler;

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
        Connection.Discovery.Server discoveryServer = new Connection.Discovery.Server("sbs|sbs-port:" + this.port);
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
        connectionHandlers.add(conn);
    }

    public void removeConn(SocketCommHandler conn) { connectionHandlers.remove(conn);}

    // Manage the connections/overall socket
    public void close(){
        connHandler.close();
    }

    public void closeConnectionIdx(int index) {
        if(connectionHandlers.size() <= index || index < 0) {
            throw new ArrayIndexOutOfBoundsException();
        } else {
            connectionHandlers.get(index).close();
            connectionHandlers.remove(index);
        }
    }

    public void closeConnection(int connNum) {
        connectionHandlers.forEach(conn -> {
            if(conn.getConnNum() == connNum) {
                conn.close();
            }
        });
    }


    public void closeAllConnections() {
        connectionHandlers.forEach(conn -> {
            conn.close();
            connectionHandlers.remove(conn);
        });
    }

    public List<Integer> getConnectionNumbers(){
        List<Integer> connNums = new ArrayList<>();

        connectionHandlers.forEach(conn -> connNums.add(conn.getConnNum()));

        return connNums;
    }
}
