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

import java.io.RandomAccessFile;
import java.util.Date;
import java.util.Scanner;

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
                                    new JsonDecoder(),
                                    new JsonEncoder(),
                                    new SimpleChannelInboundHandler<Message>() {
                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
                                            if (msg instanceof TextMessage) {
                                                System.out.println("Message from server: " + ((TextMessage) msg).getText());
                                            }
                                            if (msg instanceof FileTransferMessage) {
                                                System.out.println("New incoming file transfer message...");
                                                var message = (FileTransferMessage) msg;
                                                try (RandomAccessFile randomAccessFile = new RandomAccessFile("1", "rw")) {
                                                    randomAccessFile.seek(message.getStartPosition());
                                                    randomAccessFile.write(message.getContent());
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            if (msg instanceof EndFileTransferMessage) {
                                                System.out.println("File transfer is finished...");
                                                ctx.close();
                                            }
                                        }
                                    }
                            );
                        }
                    })
                    .option(ChannelOption.SO_KEEPALIVE, true);

            System.out.println("Клиент запущен...");
            System.out.println("Для входа или создания нового пользователя, введите \"/auth <login> <password>\".");

            ChannelFuture channelFuture = bootstrap.connect("localhost", 9000).sync();
            channelFuture.channel().writeAndFlush(createMessage());
            // Если не добавить, канал будет закрываться быстрее, чем будет получен файл!!!
            channelFuture.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            System.out.println("Клиент прервал свою работу...");
        } finally {
            System.out.println("[" + new Date() + "]" + " worker.shutdownGracefully();");
            // перенесено после блока try в получении данных
            worker.shutdownGracefully();
        }
    }

    public Object createMessage() {
        Scanner scanner = new Scanner(System.in);
        String message = scanner.nextLine();
        Object obj = null;
        if(message.startsWith("/auth")) {
            obj = new AuthMessage();
            ((AuthMessage) obj).setAuthString(message.replace("/auth", "").trim());
            System.out.println(((AuthMessage) obj).getAuthString());
            return obj;
        }
        return null;
    }
}
