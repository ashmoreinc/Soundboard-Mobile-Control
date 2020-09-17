package main.java.Connection;

public class ThreadCloseHandler extends Thread {
    public SocketCommHandler handler;

    ThreadCloseHandler (SocketCommHandler handler) {
        this.handler = handler;
    }

    @Override
    public void run () {

    }
}
