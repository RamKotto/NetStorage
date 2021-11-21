import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;

@Slf4j
public class ClientApp {
    private SocketChannel channel;
    private static final String HOST = "localhost";
    private static final int PORT = 8189;

    public Thread runApp() {
        log.debug("Подключение к серверу...");
        return new Thread(() -> {
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                Bootstrap b = new Bootstrap();
                b.group(workerGroup)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) {
                                channel = socketChannel;
                                socketChannel.pipeline().addLast(
                                        new StringDecoder(),
                                        new StringEncoder(),
                                        new ClientHandler());
                            }
                        });
                ChannelFuture future = b.connect(HOST, PORT).sync();
                future.channel().closeFuture().sync();
            } catch (InterruptedException ex) {
                log.debug("Ошибка подключения к серверу...");
            } finally {
                log.debug("Отключение...");
                workerGroup.shutdownGracefully();
            }
        });
    }

    public void sendMsg(String msg) {
        channel.writeAndFlush(msg);
    }

    public void close() {
        channel.close();
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ClientApp cp = new ClientApp();
        cp.runApp().start();
        new Thread(() -> {
            String msg;
            while (true) {
                msg = scanner.nextLine();
                if (msg.startsWith("/quit")) {
                    cp.close();
                    System.exit(-1);
                }
                cp.sendMsg(msg + "\n");
            }
        }).start();
    }
}
