package cs455.scaling;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.*;
import java.sql.Timestamp;

public class Client {
    private static SocketChannel clientSocket;
    private static ByteBuffer buffer;
    private Timer timer;

    private String serverHostName;
    private int serverPort, msgRate;
    private AtomicInteger numSent = new AtomicInteger();
    private AtomicInteger numRec = new AtomicInteger();

    private LinkedList<String> clientHashValue = new LinkedList<String>(); 
  
    public Client(String serverHostName, int serverPort, int msgRate) {
        this.serverHostName = serverHostName;
        this.serverPort = serverPort;
        this.msgRate = 1000/msgRate;
        timer = new Timer();
        PrintClient printTask = new PrintClient(this);
        timer.scheduleAtFixedRate(printTask, 0L, 20000L);
    }

    public String SHA1FromBytes(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA1"); 
        byte[] hash = digest.digest(data);
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

        ReadHandler readHandler = new ReadHandler(clientSocket);
        Thread reader = new Thread(readHandler);
        reader.start();

        while(true) {
            try {
                byte[] payload = GetByteArray(8);

                String hash = SHA1FromBytes(payload);
                storeHashValues(hash);
                numSent.incrementAndGet();

                //Put raw array into buffer and send
                buffer = ByteBuffer.wrap(payload);
                while (buffer.hasRemaining()) {
                    clientSocket.write(buffer);
                }
                
                buffer.clear();
                //Message Send Rate
                Thread.sleep(msgRate);
            } catch (Exception e) {
                System.err.println("Server connection error, stopping client");
                timer.cancel();
                clientSocket.close();    
                return;
            }
        }

        // for (int i = 0; i < 100; i++) {
        //     try {
        //         byte[] payload = GetByteArray(8);

        //         String hash = SHA1FromBytes(payload);
        //         storeHashValues(hash);
        //         numSent.incrementAndGet();

        //         System.out.println("W " + hash);

        //         //Put raw array into buffer and send
        //         buffer = ByteBuffer.wrap(payload);
        //         while (buffer.hasRemaining()) {
        //             clientSocket.write(buffer);
        //         }
                
        //         buffer.clear();
        //         //Message Send Rate
        //         Thread.sleep(500);
        //     } catch (Exception e) {
        //         e.printStackTrace();
        //     }
        // }
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
                    int header;
                    String hashValue = "";
                    //1 byte size header + 40 bytes hash string
                    ByteBuffer hashBuffer = ByteBuffer.allocate(41);
                    while (buffer.hasRemaining() && bytesRead != -1) {
                        bytesRead = clientSocket.read(hashBuffer);
                    }
                    hashBuffer.clear();

                    //Get the full payload as a string
                    String returnedMsg = new String(hashBuffer.array()).trim();
                    //Ignore an empty payload
                    if (returnedMsg.isEmpty()) {continue;}                   
                    //Get the padding amount from the header
                    header = returnedMsg.charAt(0) - '0';
                    //Get the actual hash string, ignore padding
                    hashValue = returnedMsg.substring(1,41-header);
                    
                    //If the hash matches, increment counter and remove from LL
                    if (clientHashValue.contains(hashValue)) {
                        numRec.incrementAndGet();
                        clientHashValue.remove(hashValue);                       
                    }
                    else {
                        System.out.println("INCORRECT HASH: " + hashValue);
                    }
                } catch (Exception e) {
                    System.err.println("Error reading: " + e.getMessage());
                    try {
                        clientSocket.close();
                        break;
                    } catch (Exception e1) {
                        System.err.println("Error closing socket: " + e1.getMessage());
                    }
                }
            } 
        }
    }

    public void storeHashValues(String hashValue) {
        clientHashValue.add(hashValue); 
    }
    public String getMessage(){
        return " Total Sent Count: "+this.numSent+ ", Total Recived Count: "+this.numRec;
    }
    public void resetCount(){
        this.numRec.set(0);
        this.numSent.set(0);
    }
}

class PrintClient extends TimerTask {
    Client node;
    PrintClient(Client node){
        this.node=node;
    }
    public void run() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String message = node.getMessage();
        System.out.println(timestamp+message);
        node.resetCount();
    }
}
