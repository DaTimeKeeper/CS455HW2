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
    private ThreadPoolManager manager;

    public Server(int port, int poolSize, int batchSize, int batchTime) {
        manager = new ThreadPoolManager(poolSize);
        this.port = port;
    }

    public void handleIncomingConnections() throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        
        serverSocket.bind(new InetSocketAddress(port));
        serverSocket.configureBlocking(false);
        //All connections with OP_ACCEPT will be sent to serverSocket
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
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
                    //System.out.println("Acceptable!");
                    Runnable task = () -> System.out.println(Thread.currentThread().getName() + " is working!");
                    for (int i = 0; i < 100; i++) {
                        manager.addTask(task);
                    }
                    manager.begin();
                }
                //Old connection has data
                if (key.isReadable()) {
                    System.out.println("Readable!");
                }

                //Don't loop over a key we're done with
                iter.remove();
                System.out.println("Done");
            }
        }
    }
     
}
  