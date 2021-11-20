import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class ServerApp {
    private static final int PORT = 8189;

    public static void main(String[] args) {
        // Канал для обработки подключений
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        // Канал для работы клиентов
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {

            // Настройка и создание сервера
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new StringDecoder(),
                                    new StringEncoder(),
                                    new ServerHandler());
                        }
                    });
            // Запуск сервера
            ChannelFuture future = b.bind(PORT).sync();
            // Ожидаем остановку сервера
            future.channel().closeFuture().sync();

        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
