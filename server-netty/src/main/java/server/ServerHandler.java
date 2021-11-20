package server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static server.utils.StringUtils.changeName;
import static server.utils.StringUtils.createOutputMessage;

@Slf4j
public class ServerHandler extends SimpleChannelInboundHandler<String> {
    private static final List<Channel> channels = new ArrayList<>();
    private String clientName;
    private static int newClientIndex = 1;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Client was connected..." + ctx);
        channels.add(ctx.channel());
        clientName = "Client #" + newClientIndex;
        newClientIndex++;
        broadcastMessage("SERVER", "New client connected: " + clientName);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
        String out = createOutputMessage(clientName, s);
        log.debug("Received message: " + out);
        if (s.startsWith("/")) {
            if (s.startsWith("/changename ")) {
                String newName = changeName(s);
                broadcastMessage("SERVER", clientName + " changed name to " + newName);
                clientName = newName;
            }
                return;
        }
        broadcastMessage(clientName, s);
    }

    public void broadcastMessage(String clientName, String message) {
        String out = createOutputMessage(clientName, message);
        for (Channel channel : channels) {
            channel.writeAndFlush(out);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.debug(cause.getMessage());
        channels.remove(ctx.channel());
        broadcastMessage("SERVER", clientName + " left the chat.");
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.debug("Клиент " + clientName + " left the chat.");
        channels.remove(ctx.channel());
        broadcastMessage("SERVER", clientName + " left the chat.");
        ctx.close();
    }
}
