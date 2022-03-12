package cs455.overlay;

import cs455.overlay.node.*;

public class Main {
    public static void main(String[] args) {
        try {
            if (args[1].equals("registry")){
                String regName = args[1];
                int port = Integer.parseInt(args[2]);
                int numNodes = Integer.parseInt(args[3]);
                int numMessages = Integer.parseInt(args[4]);
                Registry registry = new Registry(regName, port, numNodes, numMessages);
                registry.runRegistry();
            }
            else if (args[1].equals("messaging")) {
                //String nodeName = args[1];
                String registryHost = args[2];
                int registryPort = Integer.parseInt(args[3]);
                MessagingNode messagingNode = new MessagingNode(registryHost, registryPort);
                messagingNode.runNode();
            }
            else {
                System.out.println("Error: Incorrect args\n Use 'registry [port] [numNodes]'' or 'messaging [hostName] [hostPort]'");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
