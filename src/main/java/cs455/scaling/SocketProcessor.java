package cs455.scaling;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class SocketProcessor implements Runnable {
    Selector selector;
    ServerSocketChannel serverSocket;
    ThreadPoolManager manager;
    AtomicInteger numRec = new AtomicInteger();

    public SocketProcessor(Selector selector, ServerSocketChannel serverSocket, ThreadPoolManager manager) {
        this.selector = selector;
        this.serverSocket = serverSocket;
        this.manager = manager;
    }

    @Override
    public void run() {
        try {
            while (true) {
                selector.select();

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectedKeys.iterator();
    
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
    
                    if (key.isValid() == false) {
                        continue;
                    }
    
                    //New connection
                    if (key.isAcceptable()) {
                        register(selector, serverSocket);

                    }
                    //Registered key has data
                    if (key.isReadable()) {
                        read(key);
                    }
                    //Remove key so we don't loop over the same one
                    iter.remove();
                }
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

    private void read(SelectionKey key) {
        try {
            //ByteBuffer buffer = ByteBuffer.allocate(8);
            ByteBuffer buffer = ByteBuffer.allocate(8192);
            SocketChannel client = (SocketChannel) key.channel();
            String clientAddress = client.getRemoteAddress().toString();
            int bytesRead = 0;
            byte[] msgArray = new byte[8192];
            //byte[] msgArray = new byte[8];

            while (buffer.hasRemaining() && bytesRead != -1) {
                bytesRead += client.read(buffer);    
                //System.out.print(buffer.position());
            }
            msgArray = buffer.array();
            buffer.clear();

            if (bytesRead == -1 || bytesRead == 0) {
                System.out.println("Client disconnected");
                client.close();
            }
            else {
                numRec.incrementAndGet();
                //System.out.println(" " + numRec.get() + " " + bytesRead + " " + msgArray.length);
                HashProcessor hashProcessorTask = new HashProcessor(clientAddress, client, msgArray);
                manager.addTask(hashProcessorTask);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}