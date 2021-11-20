import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientHandler extends SimpleChannelInboundHandler<String> {

    public ClientHandler() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) {
        log.debug("Get from server: " + s);
        System.out.println(s);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
        cause.printStackTrace();
    }
}
