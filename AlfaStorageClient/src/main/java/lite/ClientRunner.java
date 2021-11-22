package lite;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.util.Date;

public class ClientRunner {

    public static void main(String[] args) throws InterruptedException {
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
                                    new ObjectEncoder(),
                                    new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                    new SimpleChannelInboundHandler<Message>() {
                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
                                            if (msg instanceof TextMessage) {
                                                var message = (TextMessage) msg;
                                                System.out.println(message.getText());
                                            }
                                            if (msg instanceof DateMessage) {
                                                var message = (DateMessage) msg;
                                                System.out.println(message.getDate());
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
                TextMessage textMessage = new TextMessage();
                textMessage.setText("This is a text message");
                channel.writeAndFlush(textMessage);

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
