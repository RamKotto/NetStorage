package lite;

import handler.JsonDecoder;
import handler.JsonEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;

public class ClientRunner {

    public static void main(String[] args) {
        new ClientRunner().start();
    }

    public void start() {
        NioEventLoopGroup worker = new NioEventLoopGroup(1);
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(worker)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(
                                    new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 3, 0, 3),
                                    new LengthFieldPrepender(3),
                                    new JsonEncoder(),
                                    new JsonDecoder(),
                                    new SimpleChannelInboundHandler<Message>() {
                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws IOException {
                                            if (msg instanceof FileMessage) {
                                                var message = (FileMessage) msg;
                                                try (RandomAccessFile randomAccessFile = new RandomAccessFile("1", "rw")) {
                                                    randomAccessFile.write(message.getContent());
                                                } finally {

                                                    // Пришлось перенести сюда, т.к. в основном блоке
                                                    // worker.shutdownGracefully(); отрабатывало быстрее,
                                                    // чем создавался файл
                                                    ctx.close();
                                                    worker.shutdownGracefully();
                                                }
                                            }
                                        }
                                    }
                            );
                        }
                    })
                    .option(ChannelOption.SO_KEEPALIVE, true);

            System.out.println("Клиент запущен...");

            Channel channel = bootstrap.connect("localhost", 9000).sync().channel();

            final DownloadFileRequestMessage message = new DownloadFileRequestMessage();
            message.setPath("C:\\Java\\NetworkStorage\\NetStorage\\test.json");
            channel.writeAndFlush(message);

        } catch (InterruptedException e) {
            System.out.println("Клиент прервал свою работу...");
        } finally {
            System.out.println("[" + new Date() + "]" + " worker.shutdownGracefully();");
            // перенесено после блока try в получении данных
//            worker.shutdownGracefully();
        }
    }
}
