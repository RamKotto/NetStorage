package lite;

import auth.AuthHandler;
import auth.User;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.RandomAccessFile;
import java.util.concurrent.Executor;

public class ServerHandler extends SimpleChannelInboundHandler<Message> {
    private static final String FILE_NAME = "C:\\JavaProjects\\NetStorage\\file";
    private static final int BUFFER_SIZE = 1024 * 64;
    // Thread pool:
    private final Executor executor;

    private AuthHandler authHandler;

    public ServerHandler(Executor executor) {
        this.executor = executor;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Client connected... " + ctx.name());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("Client inactive");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("From Server Handler exceptionCaught(): " + cause.getMessage());
        ctx.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message msg) {
        User user = null;
        if (msg instanceof AuthMessage) {
            authHandler = new AuthHandler();
            String[] loginAndPass = ((AuthMessage) msg).getAuthString().split(" ");
            user = authHandler.getOrCreateUser(loginAndPass[0], loginAndPass[1]);
            TextMessage tm = new TextMessage();
            tm.setText("Welcome " + user.getLogin() + " !");
            channelHandlerContext.writeAndFlush(tm);
            System.out.println(user.isIsAuthorized());
        }

        if (msg instanceof RequestFileMessage) {
            executor.execute(() -> {
                try (var randomAccessFile = new RandomAccessFile(FILE_NAME, "r")) {
                    final long fileLength = randomAccessFile.length();
                    // Отправит даже пустой массив. В случае, если в этом нет необходимости,
                    // избавляемся от do, и оставляем только while
                    do {
                        var position = randomAccessFile.getFilePointer();

                        // чтобы не передавать нули, в случае, остаток файла меньше буфера
                        final long availableBytes = fileLength - position;
                        byte[] bytes;
                        if (availableBytes >= BUFFER_SIZE) {
                            bytes = new byte[BUFFER_SIZE];
                        } else {
                            bytes = new byte[(int) availableBytes];
                        }

                        randomAccessFile.read(bytes);

                        final FileTransferMessage message = new FileTransferMessage();
                        message.setContent(bytes);
                        message.setStartPosition(position);

                        // channelHandlerContext.writeAndFlush(message);
                        // чтобы не перенагружать сервер - используем sync();
                        channelHandlerContext.writeAndFlush(message).sync();

                    } while (randomAccessFile.getFilePointer() < fileLength);

                    // Маркерное сообщение, для того, чтобы обозначить клиенту, что файл отправлен полностью
                    channelHandlerContext.writeAndFlush(new EndFileTransferMessage());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        if ((msg instanceof TextMessage) && user.isIsAuthorized()) {
            TextMessage message = (TextMessage) msg;
            System.out.println("incoming text message: " + message.getText());
            channelHandlerContext.writeAndFlush(msg);
        }

        if (msg instanceof DateMessage && user.isIsAuthorized()) {
            DateMessage message = (DateMessage) msg;
            System.out.println("incoming date message: " + message.getDate());
            channelHandlerContext.writeAndFlush(msg);
        }
    }
}
