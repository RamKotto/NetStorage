package lite;

import auth.ConnectionHandler;
import auth.UserTable;
import handler.JsonDecoder;
import handler.JsonEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerRunner {
    private final int PORT;

    public ServerRunner(int port) {
        this.PORT = port;
    }

    public static void main(String[] args) {
        new ServerRunner(9000).run();
    }

    public void run() {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        ExecutorService threadPool = Executors.newCachedThreadPool();
        try {
            ServerBootstrap server = new ServerBootstrap();
            server
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) {
                            ch.pipeline().addLast(
                                    new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 3, 0, 3),
                                    new LengthFieldPrepender(3),
                                    new JsonDecoder(),
                                    new JsonEncoder(),
                                    new ServerHandler(threadPool)
                            );
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future = server.bind(PORT).sync();
            System.out.println("Server is running...");
            ConnectionHandler.dbConnection();
            UserTable.createUserTableIfNotExists();
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            System.out.println("Ошибка запуска сервера...");
        } finally {
            ConnectionHandler.closeConnection();
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            threadPool.shutdownNow();
        }
    }
}
