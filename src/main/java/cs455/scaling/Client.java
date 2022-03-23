package cs455.scaling;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.ArrayList;

public class Client {
    private static SocketChannel clientSocket;
    private static ByteBuffer buffer;

    private String serverHostName, clientName;
    private int serverPort, msgRate;

    private ArrayList<String> clientHashValue = new ArrayList<String>(); 
  
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

    public String SHA1FromBytes(byte[] data) throws NoSuchAlgorithmException {

        
        MessageDigest digest = MessageDigest.getInstance("SHA1"); byte[] hash = digest.digest(data);
        BigInteger hashInt = new BigInteger(1, hash);
        return hashInt.toString(16); 
    }

    private byte[] GetByteArray(int sizeInKb)
    {
        Random rnd = new Random();
        byte[] b = new byte[sizeInKb * 1024]; // convert kb to byte
        rnd.nextBytes(b);
        return b;
    }

    public void connect() throws IOException {
        //Connect to server
        clientSocket = SocketChannel.open(new InetSocketAddress(serverHostName, serverPort));
        buffer = ByteBuffer.allocate(8192);

        for (int i = 0; i < 5; i++) {
            byte[] payload = GetByteArray(8);
            try {
                String hash = SHA1FromBytes(payload);
                storeHashValues(hash);
                System.out.println("Client hash :"+ hash);

            } catch (NoSuchAlgorithmException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            buffer = ByteBuffer.wrap(payload);

            try {
                clientSocket.write(buffer);
                buffer.clear();
            } catch (Exception e) {
                e.printStackTrace();
            }

            ByteBuffer hashBuffer = ByteBuffer.allocate(40);
            clientSocket.read(hashBuffer);
            String returnedHash = new String(hashBuffer.array()).trim();
            if (clientHashValue.contains(returnedHash)) {
                System.out.println("hash returned correctly");
            }
        }

    }

    public void storeHashValues(String hashValue) {
        clientHashValue.add(hashValue); 
    }
}
