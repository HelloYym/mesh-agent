package dubbo;

import com.google.protobuf.Message;
import communication.MessageProtos;
import dubbo.model.RpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private Channel sourceChannel;

    RpcClientHandler(Channel sourceChannel) {
        this.sourceChannel = sourceChannel;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) {
        String requestId = rpcResponse.getRequestId();
        String content = new String(rpcResponse.getBytes());

        Message response = MessageProtos.Response.newBuilder()
                .setRequestId(Integer.valueOf(requestId))
                .setContent(content)
                .build();

//        考虑不flush
        sourceChannel.writeAndFlush(response);
    }
}
