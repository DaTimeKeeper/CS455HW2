package cs455.scaling;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashProcessor implements Runnable {

    String ip;
    SocketChannel client;
    byte[] msgArray;
    SelectionKey key;

    HashProcessor(String ip, SocketChannel client, byte[] msgArray){
        this.ip = ip;
        this.client =  client;
        this.msgArray = msgArray;
    }

    @Override
    public void run() {
        try {

            ByteBuffer msgBuffer = ByteBuffer.allocate(40);
            
            //Hash the msg
            String hash = SHA1FromBytes(msgArray);
            System.out.println("Server Hash " + hash);

            //Prepare for writing
            msgBuffer = ByteBuffer.wrap(hash.getBytes());
            client.write(msgBuffer);
            msgBuffer.clear();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Hasher: Cannot write to buffer");
            e.printStackTrace();
        }


        
    }

    public String SHA1FromBytes(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA1");
        byte[] hash = digest.digest(data);
        BigInteger hashInt = new BigInteger(1, hash);
        return hashInt.toString(16); 
    }
    
}
