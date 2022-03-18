package cs455.scaling;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Server {
    private int port;

    public Server(int port, int poolSize, int batchSize, int batchTime) {
        this.port = port;
    }

    public void run() throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        
        serverSocket.bind(new InetSocketAddress(port));
        serverSocket.configureBlocking(false);
        //All connections with OP_ACCEPT will be sent to serverSocket
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        System.out.printf("Listening for connections on port %d\n", port);
        //Block until one connection accepted
        selector.select();
        System.out.println("Activity on selector!");

        //Only keys that have activity on selector
        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        Iterator<SelectionKey> iter = selectedKeys.iterator();

        while (iter.hasNext()) {
            SelectionKey key = iter.next();

            if (key.isValid() == false) {
                continue;
            }

            //New connection
            if (key.isAcceptable()) {
                System.out.println("Acceptable!");
            }
            //Old connection has data
            if (key.isReadable()) {
                System.out.println("Readable!");
            }

            //Don't loop over a key we're done with
            iter.remove();
        }
    }
     
}
  