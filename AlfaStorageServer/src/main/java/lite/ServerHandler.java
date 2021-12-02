package lite;

import FileUtils.FileUtil;
import auth.ConnectionHandler;
import auth.UserTable;
import models.User;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.io.RandomAccessFile;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Executor;

import static auth.ConnectionHandler.getConnection;

public class ServerHandler extends SimpleChannelInboundHandler<Message> {
    private static final String FILE_NAME = "C:\\JavaProjects\\NetStorage\\file";
    private static final int BUFFER_SIZE = 1024 * 64;
    // Thread pool:
    private final Executor executor;

    List<User> connectedUsers = new ArrayList<>();
    HashSet<String> TOKENS = new HashSet<>();

    private ConnectionHandler connectionHandler;

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
        cause.printStackTrace();
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
        if (msg instanceof AuthMessage) {
            String login = ((AuthMessage) msg).getAuthString().split(" ")[0];
            String pass = ((AuthMessage) msg).getAuthString().split(" ")[1];
            if (UserTable.getUserList(login, pass).size() <= 0) {
                System.out.println("Нет таких юзеров!!!");
                System.out.println("Но! Мы попробуем создать =)");
                try {
                    UserTable.createUser(login, pass);
                    User user = new User(login, pass);
                    user.setIsAuthorized(true);
                    TOKENS.add("TOKEN" + user.getLogin() + user.isIsAuthorized());
                    FileUtil.createUserDir("TOKEN" + user.getLogin() + user.isIsAuthorized());
                    TextMessage textMessage = new TextMessage();
                    textMessage.setText("TOKEN" + user.getLogin() + user.isIsAuthorized());
                    channelHandlerContext.writeAndFlush(textMessage);
                } catch (Exception ex) {
                    try {
                        getConnection().rollback();
                    } catch (SQLException rollbackEx) {
                        System.out.println("Can't getConnection().rollback() in createUser method.");
                        rollbackEx.printStackTrace();
                    }
                    TextMessage textMessage = new TextMessage();
                    textMessage.setText("Имя уже зарегестрировано. необходимо выбрать другой логин.");
                    channelHandlerContext.writeAndFlush(textMessage);
                }
            } else {
                System.out.println("Добро пожаловать " + login + "!");
                System.out.println("Доступ открыт!!!");
                User user = new User(login, pass);
                user.setIsAuthorized(true);
                TOKENS.add("TOKEN" + user.getLogin() + user.isIsAuthorized());
                FileUtil.createUserDir("TOKEN" + user.getLogin() + user.isIsAuthorized());
                TextMessage textMessage = new TextMessage();
                textMessage.setText("TOKEN" + user.getLogin() + user.isIsAuthorized());
                channelHandlerContext.writeAndFlush(textMessage);
            }
        }

        if (msg instanceof RequestFileMessage && tokenChecker(((RequestFileMessage) msg).getTOKEN())) {
            executor.execute(() -> {
                for (String fileName : ((RequestFileMessage) msg).getFiles()) {
                    try (var randomAccessFile = new RandomAccessFile(
                            FileUtil.getBasePath() + ((RequestFileMessage) msg).getTOKEN() + "\\" + fileName,
                            "r")) {
                        final long fileLength = randomAccessFile.length();
                        System.out.println(FileUtil.getBasePath() + ((RequestFileMessage) msg).getTOKEN() + "\\" + fileName);
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
                            message.setFileName(fileName);
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
                }
            });
        }

        if (msg instanceof TextMessage &&
                tokenChecker(((TextMessage) msg).getTOKEN()) &&
                ((TextMessage) msg).getText().startsWith("/files")) {
            TextMessage message = (TextMessage) msg;
            List<File> files = FileUtil.getUserFilesByToken(((TextMessage) msg).getTOKEN());
            for (File file : files) {
                TextMessage m = new TextMessage();
                m.setText(file.getName() + " - " + new Date(file.lastModified()));
                channelHandlerContext.writeAndFlush(m);
            }
            System.out.println("incoming text message: " + message.getText());
        }

        if (msg instanceof DateMessage && tokenChecker(((DateMessage) msg).getTOKEN())) {
            DateMessage message = (DateMessage) msg;
            System.out.println("incoming date message: " + message.getDate());
            channelHandlerContext.writeAndFlush(msg);
        }
    }

    private boolean tokenChecker(String token) {
        System.out.println(TOKENS);
        for (Object str : TOKENS) {
            if (str.equals(token)) {
                return true;
            }
        }
        return false;
    }
}
