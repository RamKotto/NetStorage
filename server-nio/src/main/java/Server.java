import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

@Slf4j
public class Server {
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private volatile boolean stop;

    public Server() throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(9090), 1024);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        log.debug("Сервер прослушивает порт 9090");

        while (!stop) {
            selector.select();
            log.debug("New selector event...");
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> it = selectedKeys.iterator();
            SelectionKey key = null;
            while (it.hasNext()) {
                key = it.next();
                it.remove();
                handler(key);
            }
        }
    }

    private void handler(SelectionKey key) throws IOException {
        if (key.isValid()) {
            log.debug("Key is valid");
            if (key.isAcceptable()) {
                log.debug("New acceptable event...");
                ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                SocketChannel socketChannel = serverSocketChannel.accept();
                socketChannel.configureBlocking(false);
                socketChannel.register(selector, SelectionKey.OP_READ);
            }
            if (key.isReadable()) {
                log.debug("New readable event...");
                SocketChannel socketChannel = (SocketChannel) key.channel();
                ByteBuffer readBytes = ByteBuffer.allocate(1024);
                int readCount = socketChannel.read(readBytes);
                if (readCount > 0) {
                    readBytes.flip();
                    byte[] bytes = new byte[readBytes.remaining()];
                    readBytes.get(bytes);
                    String receiveMsg = new String(bytes, "UTF-8");
                    if ("bye".equals(receiveMsg)) {
                        stop();
                        return;
                    }
                    String responseString = "Сервер получил:" + receiveMsg;
                    log.debug(responseString);
                    responseString = "Сервер вернул: " + receiveMsg;
                    log.debug(responseString);
                    ByteBuffer responseBuffer = ByteBuffer.allocate(responseString.getBytes().length);
                    responseBuffer.put(responseString.getBytes());
                    responseBuffer.flip();
                    socketChannel.write(responseBuffer);
                }
            }
        }
    }

    public void stop() {
        this.stop = true;
    }

    public static void main(String[] args) {
        try {
            new Server();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
