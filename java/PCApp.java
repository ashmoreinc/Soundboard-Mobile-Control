import Connection.Client;
import Connection.Server;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class PCApp {

    public static void main(String[] args) {
        Scanner read = new Scanner(System.in);

        System.out.println("Server(s) or client(c)? ");

        String input = read.nextLine();

        if(input.equals("c")){
            // Get the possible servers
            List<InetAddress> servers = Connection.Discovery.Client.getServers();

            if (servers == null) {
                System.out.println("An error occurred while fetching the servers.");
            } else {
                System.out.println("Servers available: ");
                AtomicInteger index = new AtomicInteger();
                servers.forEach(serv -> {
                    System.out.println(index + "\t||\t" + serv);
                    index.addAndGet(1);
                });
                System.out.print("Server choice: ");

                int option = read.nextInt();

                if(option < servers.size() && option >=0){
                    InetAddress addr = servers.get(option);
                    runClient(9001, addr.getHostName());
                } else {
                    System.out.println("Server choice was out of range.");
                }
            }
        } else if(input.equals("s")){
            runServer();
        } else {
            System.out.println("Invalid option.");
        }
    }

    private static void runServer(){
        Server server = new Server();
        server.start();

        Scanner sc = new Scanner(System.in);
        String input;
        boolean running = true;
        while(running) {
            input = sc.nextLine();

            switch (input) {
                case "exit" ->  {
                    running = false;
                    server.close();
                }
                case "remove all" -> server.closeAllConnections();
                case "get conns" -> {
                    List<Integer> connNums = server.getConnectionNumbers();

                    if (connNums.size() != 0) {
                        System.out.print("Connection Numbers: ");
                        connNums.forEach(num -> System.out.print(num + ", "));
                        System.out.println();
                    } else {
                        System.out.println("No active connections.");
                    }

                }
            }
        }
    }

    private static void runClient(int port, String hostname){
        System.out.println("Initialising Client.");
        Client client = new Client(port, hostname);

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
