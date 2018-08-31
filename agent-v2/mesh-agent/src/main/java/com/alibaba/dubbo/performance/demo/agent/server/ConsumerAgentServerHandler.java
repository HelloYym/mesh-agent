package com.alibaba.dubbo.performance.demo.agent.server;

import com.alibaba.dubbo.performance.demo.agent.proto.Agent;
import com.alibaba.dubbo.performance.demo.agent.rpc.ConsumerRpcClient;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: chenyifan
 * Date: 2018-05-23
 * Time: 下午3:49
 */
public class ConsumerAgentServerHandler extends ChannelInboundHandlerAdapter {

    private Logger logger = LoggerFactory.getLogger(ConsumerAgentServerHandler.class);

    private static final String METHOD = "method";
    private static final String INTERFACE = "interface";
    private static final String PARAMETER_TYPE = "parameterTypesString";
    private static final String PARAMETER = "parameter";

    private ConsumerRpcClient client;

    private long channelId;

    private Channel targetChannel;

    public ConsumerAgentServerHandler(ConsumerRpcClient client) {
        this.client = client;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            Map<String, String> pMap = parse((FullHttpRequest) msg);

            Agent.AgentRequest request = Agent.AgentRequest.newBuilder()
                    .setId(channelId)
                    .setMethodName(pMap.get(METHOD))
                    .setInterfaceName(pMap.get(INTERFACE))
                    .setParameterTypesString(pMap.get(PARAMETER_TYPE))
                    .setParameter(pMap.get(PARAMETER)).build();

            targetChannel.writeAndFlush(request, targetChannel.voidPromise());
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        channelId = IdGenerator.getInstance().getChannelId();
        ConsumerAgentServer.channels.put(channelId, ctx.channel());
        targetChannel = client.getChannel(ctx.channel().eventLoop());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelInactive();
        ConsumerAgentServer.channels.remove(channelId);
    }

    private Map<String, String> parse(FullHttpRequest fullReq) throws IOException {


        Map<String, String> parmMap = new HashMap<>();


        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(fullReq);
        decoder.offer(fullReq);

        List<InterfaceHttpData> paramList = decoder.getBodyHttpDatas();

        for (InterfaceHttpData param : paramList) {

            Attribute data = (Attribute) param;
            parmMap.put(data.getName(), data.getValue());
        }
        decoder.cleanFiles();
        decoder.destroy();

        return parmMap;
    }

}
