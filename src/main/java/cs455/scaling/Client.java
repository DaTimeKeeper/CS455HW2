package cs455.scaling;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Client {
    private static SocketChannel clientSocket;
    private static ByteBuffer buffer;

    private String serverHostName, clientName;
    private int serverPort, msgRate;
  
    public Client(String serverHostName, int serverPort, int msgRate) {
        this.serverHostName = serverHostName;
        this.serverPort = serverPort;
        this.msgRate = msgRate;
    }
      
    public Client(String serverHostName, int serverPort, int msgRate, String clientName) {
        this.serverHostName = serverHostName;
        this.serverPort = serverPort;
        this.msgRate = msgRate;
        //this.clientName = clientName;
    }

    public void connect() throws IOException {
        //Connect to server
        clientSocket = SocketChannel.open(new InetSocketAddress(serverHostName, serverPort));
        buffer = ByteBuffer.allocate(256);

        buffer = ByteBuffer.wrap("Client".getBytes());
        try {
            clientSocket.write(buffer);
            buffer.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
