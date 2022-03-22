package cs455.scaling;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Server {
    private int port;
    private ThreadPoolManager manager;

    public Server(int port, int poolSize, int batchSize, int batchTime) {
        manager = new ThreadPoolManager(poolSize);
        manager.begin();
        this.port = port;
    }

    public void handleIncomingConnections() throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        
        serverSocket.bind(new InetSocketAddress(port));
        serverSocket.configureBlocking(false);
        //All connections with OP_ACCEPT will be sent to serverSocket
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        System.out.printf("Listening for connections on port %d\n", port);
        
        while (true) {
            selector.select();
            System.out.println("Activity on selector!");

            //Only keys that have activity on selector will be used
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();

            //AcceptConn acceptConn = new AcceptConn(iter, serverSocket, selector);
           // manager.addTask(acceptConn);

            // while (iter.hasNext()) {
            //     SelectionKey key = iter.next();
    
            //     if (key.isValid() == false) {
            //         continue;
            //     }
    
            //     //New connection
            //     if (key.isAcceptable()) {
            //         register(selector, serverSocket);
            //     }
            //     //Registered key has data
            //     if (key.isReadable()) {
            //         read(key);
            //     }
    
            //     //Remove key so we don't loop over the same one
            //     iter.remove();
            // }
        }
    }

    private void read(SelectionKey key) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(256);
            SocketChannel client = (SocketChannel) key.channel();
            int bytesRead = client.read(buffer);

            if (bytesRead == -1) {
                client.close();
                System.out.println("Client disconnected");
            }
            else {
                System.out.println("Recieved " + new String(buffer.array()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Register channel to OP.READ, will activate key is socket has data
    private void register(Selector selector, ServerSocketChannel serverSocket) {
        try {
            SocketChannel client = serverSocket.accept();
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
            System.out.println("Registered client");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
  