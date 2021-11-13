package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class EchoServer {

    public static void main(String[] args) {
        EchoServer echoServer = new EchoServer();
        try {
            echoServer.serverRun();
        } catch (IOException e) {
            System.out.println("Server stopped!");
        }
    }

    public void serverRun() throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress("localhost", 9000));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server is running...");

        while(true) {
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                if (selectionKey.isAcceptable()) {
                    System.out.println("Selector acceptable...");
                    register(selector, serverSocketChannel);
                }
                if (selectionKey.isReadable()){
                    System.out.println("Selector is readable...");
                    readMessage(selectionKey);
                }
                iterator.remove();
            }
        }
    }

    public void register(Selector selector, ServerSocketChannel serverSocketChannel) throws IOException {
        SocketChannel client = serverSocketChannel.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        System.out.println("New client connected.");
    }

    public void readMessage(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(256);
        client.read(byteBuffer);
        String message = new String(byteBuffer.array());
        System.out.println("Message: " + message + "; Thread name: " + Thread.currentThread().getName());
    }
}
