package cs455.scaling;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashProcessor implements Runnable {

    String ip;
    SocketChannel client;
    ByteBuffer message;

    HashProcessor(String ip, SocketChannel client, ByteBuffer message){
        this.ip = ip;
        this.client =  client;
        this.message = message;


    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        try {
            String hash = SHA1FromBytes(message.array());
        
            System.out.println("Server Hash " + hash  );


        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
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
