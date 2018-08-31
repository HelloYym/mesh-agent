package communication;

import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import server.ConsumerAgentServer;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;


public class ProviderAgentClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {

        MessageProtos.Response response = (MessageProtos.Response) msg;

        Long requestId = response.getRequestId();
        Channel channel = ConsumerAgentServer.channelMap.get(requestId);
        if (channel == null) {
            throw new Exception("request channel is null");
        }
        ConsumerAgentServer.channelMap.remove(requestId);

        FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(response.getContent().getBytes()));
        resp.headers().set(HttpHeaderNames.CONTENT_LENGTH, resp.content().readableBytes());
        resp.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        channel.writeAndFlush(resp);

        ReferenceCountUtil.release(msg);
    }
}
