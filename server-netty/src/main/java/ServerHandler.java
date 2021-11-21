import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static utils.StringUtils.changeName;
import static utils.StringUtils.createOutputMessage;

@Slf4j
public class ServerHandler extends SimpleChannelInboundHandler<String> {
    private static final List<Channel> channels = new ArrayList<>();
    private String clientName;
    private static int newClientIndex = 1;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.debug("Client was connected..." + ctx);
        channels.add(ctx.channel());
        clientName = "Client #" + newClientIndex;
        newClientIndex++;
        broadcastMessage("SERVER", "New client connected: " + clientName);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) {
        String out = createOutputMessage(clientName, s);
        log.debug("Received message: " + out);
        if (s.startsWith("/")) {
            if (s.startsWith("/changename ")) {
                String newName = changeName(s);
                broadcastMessage("SERVER", clientName + " changed name to " + newName);
                clientName = newName;
            } else if (s.startsWith("/quit")) {
                channelHandlerContext.close();
            }
            return;
        }
        broadcastMessage(clientName, s);
    }

    public void broadcastMessage(String clientName, String message) {
        String out = createOutputMessage(clientName, message);
        for (Channel channel : channels) {
            channel.writeAndFlush(new Date() + " " +  out);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.debug(cause.getMessage());
        channels.remove(ctx.channel());
        broadcastMessage("SERVER", clientName + " left the chat.");
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.debug("Клиент " + clientName + " left the chat.");
        channels.remove(ctx.channel());
        broadcastMessage("SERVER", clientName + " left the chat.");
        ctx.close();
    }
}
