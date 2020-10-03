package Connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;


public class SocketCommHandler extends Thread{
    private List<String> messages = new ArrayList<>();

    private final Socket sock;
    private final PrintWriter out;
    private final BufferedReader in;

    private final messageIn onRecv;

    public boolean running;

    private int connNum;
    public static List<SocketCommHandler> activeComms = new ArrayList<>();

    private static final String CLOSE_CONN_MSG = "CLOSECONN";

    private clientClosed onClose;

    SocketCommHandler (Socket socket, PrintWriter output, BufferedReader input, int num, messageIn onRecv, clientClosed onClose) {
        sock = socket;
        out = output;
        in = input;

        connNum = num;

        this.onRecv = onRecv;
        this.onClose = onClose;
        running = false;
    }

    // Thread function.
    @Override
    public void run(){
        // Receive messages and add them to a list.
        activeComms.add(this);

        String recv;
        running = true;
        while (running) {
            try {
                if ((recv = in.readLine()) == null) {
                    running = false;
                    break;
                }
            } catch (SocketException e) {
                System.out.println("Connection reset (" + connNum + ").");
                running = false;
                break;
            } catch (IOException e) {
                System.err.println("Exception caught BufferedReader.readline()");
                e.printStackTrace();
                running = false;
                break;
            } catch (Exception e) {
                System.err.println("Unknown exception caught.");
                e.printStackTrace();
                running = false;
                break;
            }

            if(recv.equals(CLOSE_CONN_MSG)){
                close();
                break;
            }

            onRecv.messageRecv(recv, connNum);
        }

        System.out.println("Connection (" + connNum + ") closed.");
        close();
    }

    // Close the connections
    public void close(){
        sendCloseMessage();
        try {
            running = false;
            sock.close();
            out.close();
            in.close();

            activeComms.remove(this);
        } catch (Exception e){
            System.err.println("Error closing connections.");
            e.printStackTrace();
        }

        if(onClose != null) onClose.onClose(this);
    }

    // Send Message
    public void sendMessage(String message) {
        out.println(message);

        // TODO: Handle failures
    }

    private void sendCloseMessage(){
        sendMessage(CLOSE_CONN_MSG);
    }

    // Get the longest waiting message (first stored)
    public String getMessage (){
        if(!messages.isEmpty()) {
            String msg =  messages.get(0);
            messages.remove(0);
            return msg;
        } else {
            return null;
        }
    }

    // Returns the most recent message (last stored)
    public String getLastMessage(){
        int size = messages.size();

        if(size > 0) {
            String msg = messages.get(size-1);
            messages.remove(size - 1);
            return msg;
        } else {
            return null;
        }
    }

    // Flush the list
    public void clearMessages() {
        messages.clear();
    }

    // End the task
    public void end (){
        running = false;
    }

    // Getters
    public int getConnNum () {
        return connNum;
    }
}
