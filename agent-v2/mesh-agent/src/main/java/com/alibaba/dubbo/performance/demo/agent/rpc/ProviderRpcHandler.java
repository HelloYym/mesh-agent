package com.alibaba.dubbo.performance.demo.agent.rpc;

import com.alibaba.dubbo.performance.demo.agent.proto.Agent;
import com.alibaba.dubbo.performance.demo.agent.rpc.model.RpcResponse;
import com.alibaba.dubbo.performance.demo.agent.server.ProviderAgentServer;
import com.google.protobuf.ByteString;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: chenyifan
 * Date: 2018-05-24
 * Time: 下午10:00
 */
public class ProviderRpcHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private Logger logger = LoggerFactory.getLogger(ProviderRpcHandler.class);

    private Channel sourceChannel;

    ProviderRpcHandler(Channel sourceChannel) {
        this.sourceChannel = sourceChannel;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {

        Agent.AgentResponse agentResponse = Agent.AgentResponse.newBuilder()
                .setId(response.getRequestId())
                .setValueBytes(ByteString.copyFrom(response.getBytes()))
                .build();

        sourceChannel.writeAndFlush(agentResponse, sourceChannel.voidPromise());
    }

}
