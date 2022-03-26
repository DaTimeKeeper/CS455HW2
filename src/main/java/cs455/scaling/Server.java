package cs455.scaling;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

public class Server {
    private int port;
    private ThreadPoolManager manager;

    public Server(int port, int poolSize, int batchSize, int batchTime) {
        manager = new ThreadPoolManager(poolSize, batchSize);
        Thread threadPool = new Thread(manager, "ThreadPool");
        threadPool.start();
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
}
  