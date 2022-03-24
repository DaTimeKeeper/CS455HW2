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

            ByteBuffer msgBuffer = ByteBuffer.allocate(41);
            
            //Hash the msg
            String hash = SHA1FromBytes(msgArray);

            byte diff = (byte) ((byte)40 - hash.length());
            //Add diff header to denote hash length
            String payload = Byte.toString(diff) + hash;
            //Pad to length of 40
            System.out.println(payload);
            payload = pad(payload, diff);
            System.out.println(payload + '\n');
            //System.out.println("Server Hash " + hash);
            //System.out.println(hash.length() + " " + hash);

            //Prepare for writing
            msgBuffer = ByteBuffer.wrap(payload.getBytes());
            while (msgBuffer.hasRemaining()) {
                client.write(msgBuffer);
            }
            msgBuffer.clear();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Hasher: Cannot write to buffer");
            e.printStackTrace();
        }


        
    }

    public String pad(String payload, byte diff) {
        String pad = Byte.toString((byte)0);
        int padLength = (int) diff;
        
        for (int i = 0; i < padLength; i++) {
            payload += pad;
        }

        return payload;
    }

    public String SHA1FromBytes(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA1");
        byte[] hash = digest.digest(data);
        BigInteger hashInt = new BigInteger(1, hash);
        return hashInt.toString(16); 
    }
}
