package javatutor.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class SelectorTest {
    private static int PORT = 9876;

    public static void main(String[] args) {
        int BUFFER_SIZE = 1024;
        ByteBuffer sharedBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
        Selector selector = null;
        ServerSocket serverSocket = null;
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocket = serverSocketChannel.socket();
            InetSocketAddress inetSocketAddress = new InetSocketAddress(PORT);
            serverSocket.bind(inetSocketAddress);
            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            System.err.println("Unable to setup environment");
            System.exit(-1);
        }
        try {
            while (true) {
                int count = selector.select();
                // нечего обрабатывать
                if (count == 0) {
                    continue;
                }
                Set<SelectionKey> keySet = selector.selectedKeys();
                Iterator<SelectionKey> itor = keySet.iterator();
                while (itor.hasNext()) {
                    SelectionKey selectionKey = itor.next();
                    itor.remove();
                    Socket socket = null;
                    SocketChannel channel = null;
                    if (selectionKey.isAcceptable()) {
                        System.out.println("Got acceptable key");
                        try {
                            socket = serverSocket.accept();
                            System.out.println("Connection from: " + socket);
                            channel = socket.getChannel();
                        } catch (IOException e) {
                            System.err.println("Unable to accept channel");
                            e.printStackTrace();
                            selectionKey.cancel();
                        }
                        if (channel != null) {
                            try {
                                System.out.println("Watch for something to read");
                                channel.configureBlocking(false);
                                channel.register(selector, SelectionKey.OP_READ);
                            } catch (IOException e) {
                                System.err.println("Unable to use channel");
                                e.printStackTrace();
                                selectionKey.cancel();
                            }
                        }
                    }
                    if (selectionKey.isReadable()) {
                        System.out.println("Reading channel");
                        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                        sharedBuffer.clear();
                        int bytes = -1;
                        try {
                            while (
                                    (bytes = socketChannel.read(sharedBuffer)) > 0) {
                                System.out.println("Reading...");
                                sharedBuffer.flip();
                                while (sharedBuffer.hasRemaining()) {
                                    System.out.println("Writing...");
                                    socketChannel.write(sharedBuffer);
                                }
                                sharedBuffer.clear();
                            }
                        } catch (IOException e) {
                            System.err.println("Error writing back bytes");
                            e.printStackTrace();
                            selectionKey.cancel();
                        }
                        try {
                            System.out.println("Closing...");
                            socketChannel.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            selectionKey.cancel();
                        }
                    }
                    System.out.println("Next...");
                }
            }
        } catch (IOException e) {
            System.err.println("Error during select()");
            e.printStackTrace();
        }
    }
}
