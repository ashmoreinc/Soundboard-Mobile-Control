package main.java;

import main.java.Connection.Client;
import main.java.Connection.Server;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class main {
    private static String HostName = "localhost";
    private static int PortNumber = 9001;

    // 0 = server, 1 = client
    private static int mode = 0;

    public static void main(String[] args) throws IOException {

        Scanner sc= new Scanner(System.in);
        System.out.print("Run as Server (0) or Client (1): ");

        mode = sc.nextInt();

        System.out.println("Mode chosen: " + mode);

        switch (mode) {
            case 0 -> runServer();
            case 1 -> runClient();
            default -> System.out.println("Mode is incorrect. Exiting.");
        }
    }

    private static void runServer() throws  IOException{
        Server server = new Server();
        server.start();

        Scanner sc = new Scanner(System.in);
        String input;
        while(true) {
            input = sc.nextLine();

            switch (input) {
                case "exit" -> server.close();
                case "remove all" -> server.closeAllConnections();
                case "get conns" -> {
                    List<Integer> connNums = server.getConnectionNumbers();

                    if (connNums.size() != 0) {
                        System.out.print("Connection Numbers: ");
                        connNums.forEach(num -> {
                            System.out.print(num + ", ");
                        });
                        System.out.println();
                    } else {
                        System.out.println("No active connections.");
                    }

                }
            }
        }
    }

    private static void runClient(){
        System.out.println("Initialising Client.");
        Client client = new Client(PortNumber, HostName);

        client.start();

        Scanner sc = new Scanner(System.in);
        String input;
        while(true) {
            input = sc.nextLine();

            if(input.equals("exit")){
                client.close();
                break;
            } else {
                client.commHandler.sendMessage(input);
            }
        }
    }
}
