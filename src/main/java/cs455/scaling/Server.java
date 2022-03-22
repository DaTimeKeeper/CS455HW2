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

        serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        System.out.printf("Listening for connections on port %d\n", port);

        SocketProcessor socketProcessor = new SocketProcessor(selector, serverSocket, manager);
        manager.addTask(socketProcessor);
        
        
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
                System.out.println("Recieved: " + new String(buffer.array()));
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
            System.out.println("Registered client " + selector.selectedKeys().size());
            Thread.sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
  