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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;

public class Client {
    private static SocketChannel clientSocket;
    private static ByteBuffer buffer;

    private String serverHostName, clientName;
    private int serverPort, msgRate;
    private AtomicInteger numSent = new AtomicInteger();
    private AtomicInteger numRec = new AtomicInteger();

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
        MessageDigest digest = MessageDigest.getInstance("SHA1"); 
        byte[] hash = digest.digest(data);
        //System.out.println("Size " + hash.length);
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
        //buffer = ByteBuffer.allocate(8);

        ReadHandler readHandler = new ReadHandler(clientSocket);
        Thread reader = new Thread(readHandler);
        reader.start();

        for (int i = 0; i < 100; i++) {
            try {
                byte[] payload = GetByteArray(8);

                //byte[] payload = {1,2,3,4,5,6,7,8};

                String hash = SHA1FromBytes(payload);
                //System.out.println(hash.length());
                storeHashValues(hash);
                //System.out.println(hash);

                numSent.incrementAndGet();

                buffer = ByteBuffer.wrap(payload);

                //while (buffer.hasRemaining()) {
                    clientSocket.write(buffer);
                //}

                //System.out.println(writeSize);
                
                buffer.clear();

                

                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class ReadHandler implements Runnable {
        SocketChannel clientSocket;

        ReadHandler(SocketChannel clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    int bytesRead = 0;
                    ByteBuffer hashBuffer = ByteBuffer.allocate(41);
                    while (buffer.hasRemaining() && bytesRead != -1) {
                        bytesRead = clientSocket.read(hashBuffer);
                    }
                    

                    String returnedMsg = new String(hashBuffer.array()).trim();
                    char header = returnedMsg.charAt(0);
                    System.out.println(header + " " + returnedMsg);
                    //String hashValue = returnedMsg.substring(1,40-header);
                    //System.out.println("R " + returnedHash);
                    if (clientHashValue.contains(returnedMsg)) {
                        numRec.incrementAndGet();
                    }
                    else {
                        //System.out.println("Incorrect hash");
                    }
                    hashBuffer.clear();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } 
        }
    }

    public void storeHashValues(String hashValue) {
        clientHashValue.add(hashValue); 
    }
}
