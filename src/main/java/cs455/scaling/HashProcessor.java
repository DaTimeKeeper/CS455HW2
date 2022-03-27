package cs455.scaling;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class HashProcessor implements Runnable {

    ArrayList<Message> messages;

    HashProcessor (ArrayList<Message> messages) {
        this.messages = messages; 
    }

    @Override
    public void run() {
        try {

            for (Message msg : messages) {
                SocketChannel client = msg.client;
                byte[] msgArray = msg.msgArray;
                ByteBuffer msgBuffer = ByteBuffer.allocate(41);
            
                //Hash the msg
                String hash = SHA1FromBytes(msgArray);

                byte diff = (byte) ((byte)40 - hash.length());
                //Add padding amt as a header
                String payload = Byte.toString(diff) + hash;
                //Pad to length of 40
                payload = pad(payload, diff);

                //Prepare for writing
                msgBuffer = ByteBuffer.wrap(payload.getBytes());
                while (msgBuffer.hasRemaining()) {
                    client.write(msgBuffer);
                }
                msgBuffer.clear();
            }

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Hasher: Cannot write to buffer");
            e.printStackTrace();
        }


        
    }

    //Right pad with 0 to length 40
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
