import Connection.Client;
import Connection.Server;
import Sound.SoundManager;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class PCApp {

    public static void main(String[] args) {
        try {
            SoundManager sound = new SoundManager();

            Map<String, String> sounds = sound.getSounds();

            sounds.forEach((name, filename) -> System.out.println(name + " || " + filename));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void beginConnection () {
        Scanner sc= new Scanner(System.in);
        System.out.print("Run as Server (0) or Client (1): ");

        // 0 = server, 1 = client
        int mode = sc.nextInt();

        System.out.println("Mode chosen: " + mode);

        switch (mode) {
            case 0 -> runServer();
            case 1 -> runClient();
            default -> System.out.println("Mode is incorrect. Exiting.");
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

    private static void runClient(){
        System.out.println("Initialising Client.");
        int portNumber = 9001;
        String hostName = "localhost";
        Client client = new Client(portNumber, hostName);

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
