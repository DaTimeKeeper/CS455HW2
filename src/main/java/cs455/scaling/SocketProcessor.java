package cs455.scaling;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.*;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.concurrent.ConcurrentHashMap;

public class SocketProcessor implements Runnable {
    Selector selector;
    ServerSocketChannel serverSocket;
    ThreadPoolManager manager;
    ConcurrentHashMap<String,AtomicInteger> hm = new ConcurrentHashMap<String,AtomicInteger>();

    public SocketProcessor(Selector selector, ServerSocketChannel serverSocket, ThreadPoolManager manager) {
        this.selector = selector;
        this.serverSocket = serverSocket;
        this.manager = manager;
        Timer timer = new Timer();
        PrintServer printTask = new PrintServer(this);
        timer.scheduleAtFixedRate(printTask, 0L, 20000L);
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
            this.hm.putIfAbsent(client.getRemoteAddress().toString(), new AtomicInteger(0));
        } catch (Exception e) {
            System.err.println("Register error: " + e.getMessage());
        }
    }

    private void read(SelectionKey key) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(8192);
            SocketChannel client = (SocketChannel) key.channel();
            String clientAddress = client.getRemoteAddress().toString();
            int bytesRead = 0;
            byte[] msgArray = new byte[8192];

            while (buffer.hasRemaining() && bytesRead != -1) {
                bytesRead += client.read(buffer);    
            }
            msgArray = buffer.array();
            buffer.clear();

            if (bytesRead == -1 || bytesRead == 0) {
                System.out.println("Client disconnected");
                client.close();
            }
            else {
                //Increment counter if read successful
                hm.get(clientAddress).incrementAndGet();

                //HashProcessor hashProcessorTask = new HashProcessor(clientAddress, client, msgArray);
                manager.addHash(new Message(client, msgArray));
            }
        } catch (Exception e) {
            System.err.println("Read error: " + e.getMessage());
        }
    }

    public String getMessage(){
        DecimalFormat decFormat = new DecimalFormat("0.0##");
        double x=0;
        int y=hm.size();
        double p=0;
        double q=0;
        for(Map.Entry mapElement : hm.entrySet()){
            AtomicInteger temp = (AtomicInteger)mapElement.getValue();
            x+=temp.get();
        }
        x=x/20;
        if(y>0){
            p=x/y;
        }
        for(Map.Entry mapElement : hm.entrySet()){
            AtomicInteger temp = (AtomicInteger)mapElement.getValue();
            //diff = (client avg over 20s) - (average of all clients)
            double diff = (temp.get()/20) - p;
            q+=Math.pow(diff,2);
        }
        if(y>0){
            q=q/y;
        }
        q=Math.sqrt(q);
        
        return "Server Throughput: "+decFormat.format(x)+" messages/s, Active Client Connections: "+y+
        "\n" + "Mean Per-client Throughput: "+decFormat.format(p)+" messages/s, Std. Dev. Of Per-client Throughput: "+decFormat.format(q)+" messages/s";
    }
    public void resetCount(){
        for(Map.Entry mapElement : hm.entrySet()){
            String temp = (String)mapElement.getKey();
            hm.get(temp).set(0);
        }
    }
}
class PrintServer extends TimerTask {
    SocketProcessor node;
    PrintServer(SocketProcessor node){
        this.node=node;
    }
    public void run() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String message = node.getMessage();
        System.out.println(timestamp + "\n" + message + '\n');
        node.resetCount();
    }
}