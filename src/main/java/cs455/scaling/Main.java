package cs455.scaling;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {  
            if (args[1].equals("server")){
                int port = Integer.parseInt(args[2]);
                int poolSize = Integer.parseInt(args[3]);
                int batchSize = Integer.parseInt(args[4]);
                int batchTime = Integer.parseInt(args[5]);
                
                Server server = new Server(port, poolSize, batchSize, batchTime);
                server.handleIncomingConnections();
            }
            else if (args[1].equals("client")) {
                String serverHostName = args[2];
                int serverPort = Integer.parseInt(args[3]);
                int msgRate = Integer.parseInt(args[4]);
               // String clientName = args[5];
                Client client = new Client(serverHostName, serverPort, msgRate);
                //Client client = new Client(serverHostName, serverPort, msgRate);
                client.connect();
            }
            else {
                System.out.println("Error: Incorrect args\n Use 'server [portnum] [thread-pool-size] [batch-size] [batch-time]' or 'client [server-host] [server-port] [message-rate]'");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
