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
                                        protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
                                            if (msg instanceof TextMessage) {
                                                TextMessage message = (TextMessage) msg;
                                                System.out.println("From server: " + message.getText());
                                            }
                                            if (msg instanceof DateMessage) {
                                                DateMessage message = (DateMessage) msg;
                                                System.out.println("From server: " + message.getDate());
                                            }
                                        }
                                    }
                            );
                        }
                    })
                    .option(ChannelOption.SO_KEEPALIVE, true);

            System.out.println("Клиент запущен...");

            Channel channel = bootstrap.connect("localhost", 9000).sync().channel();

            while (true) {
                TextMessage message = new TextMessage();
                message.setText("This is a text message");
                channel.writeAndFlush(message);

                DateMessage dateMessage = new DateMessage();
                dateMessage.setDate(new Date());
                channel.writeAndFlush(dateMessage);

                Thread.sleep(2000);
            }

        } catch (InterruptedException e) {
            System.out.println("Клиент прервал свою работу...");
        } finally {
            worker.shutdownGracefully();
        }
    }
}
