package cs455.scaling;

import java.nio.channels.SocketChannel;

public class Message {
    SocketChannel client;
    byte[] msgArray;

    public Message(SocketChannel client, byte[] msgArray) {
        this.client = client;
        this.msgArray = msgArray;
    }
}
