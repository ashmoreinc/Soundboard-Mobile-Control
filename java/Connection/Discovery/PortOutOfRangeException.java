package Connection.Discovery;

public class PortOutOfRangeException extends Exception{
    public PortOutOfRangeException() {
        super("Port must satisfy 0 <= port <= 65535.");
    }
}
