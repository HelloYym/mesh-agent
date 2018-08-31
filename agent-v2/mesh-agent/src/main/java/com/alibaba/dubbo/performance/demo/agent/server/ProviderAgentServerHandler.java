package com.alibaba.dubbo.performance.demo.agent.server;

import com.alibaba.dubbo.performance.demo.agent.proto.Agent;
import com.alibaba.dubbo.performance.demo.agent.rpc.ProviderRpcClient;
import com.alibaba.dubbo.performance.demo.agent.rpc.model.JsonUtils;
import com.alibaba.dubbo.performance.demo.agent.rpc.model.Request;
import com.alibaba.dubbo.performance.demo.agent.rpc.model.RpcInvocation;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: chenyifan
 * Date: 2018-05-23
 * Time: 下午3:38
 */
public class ProviderAgentServerHandler extends ChannelInboundHandlerAdapter {

    private Logger logger = LoggerFactory.getLogger(ProviderAgentServerHandler.class);

    private static final String REQUEST_VERSION = "2.0.0";

    private ProviderRpcClient client;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Agent.AgentRequest agentRequest = (Agent.AgentRequest) msg;

        Long requestId = agentRequest.getId();
        String interfaceName = agentRequest.getInterfaceName();
        String method = agentRequest.getMethodName();
        String parameterTypesString = agentRequest.getParameterTypesString();
        String parameter = agentRequest.getParameter();

        client.write(requestId, interfaceName, method, parameterTypesString, parameter);

        ReferenceCountUtil.release(msg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        client = new ProviderRpcClient(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelInactive();
        client.deactivate();
    }


}
