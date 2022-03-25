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
import java.util.*;
import java.sql.Timestamp;

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
        Timer timer = new Timer();
        PrintClient printTask = new PrintClient(this);
        timer.scheduleAtFixedRate(printTask, 0L, 20000L);
    }
    public Client(String serverHostName, int serverPort, int msgRate, String clientName) {
        this.serverHostName = serverHostName;
        this.serverPort = serverPort;
        this.msgRate = msgRate;
        Timer timer = new Timer();
        PrintClient printTask = new PrintClient(this);
        timer.scheduleAtFixedRate(printTask, 0L, 20000L);
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
        ReadHandler readHandler = new ReadHandler(clientSocket);
        Thread reader = new Thread(readHandler);
        reader.start();

        for (int i = 0; i < 100; i++) {
            try {
                byte[] payload = GetByteArray(8);

                //byte[] payload = {1,2,3,4,5,6,7,8};

                String hash = SHA1FromBytes(payload);
                storeHashValues(hash);
                //System.out.println("Client hash :"+ hash);
                System.out.println("W " + hash);

                numSent.incrementAndGet();

                buffer = ByteBuffer.wrap(payload);

                int writeSize = clientSocket.write(buffer);

                //System.out.println(writeSize);
                
                buffer.clear();

                

                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            reader.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //System.out.println(numSent.get() + " " + numRec.get());
    }

    private class ReadHandler implements Runnable {
        SocketChannel clientSocket;

        ReadHandler(SocketChannel clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            //System.out.println("start");
            while (true) {
                try {
                    ByteBuffer hashBuffer = ByteBuffer.allocate(40);
                    int readSize = clientSocket.read(hashBuffer);
                    //System.out.println(readSize);

                    String returnedHash = new String(hashBuffer.array()).trim();
                    System.out.println("R " + returnedHash);
                    if (clientHashValue.contains(returnedHash)) {
                        System.out.println("hash returned correctly");
                        numRec.incrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } 
            //System.out.println("end");

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
