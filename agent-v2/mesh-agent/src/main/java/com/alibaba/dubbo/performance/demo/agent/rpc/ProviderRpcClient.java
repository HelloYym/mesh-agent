package com.alibaba.dubbo.performance.demo.agent.rpc;

import com.alibaba.dubbo.performance.demo.agent.rpc.model.JsonUtils;
import com.alibaba.dubbo.performance.demo.agent.rpc.model.Request;
import com.alibaba.dubbo.performance.demo.agent.rpc.model.RpcInvocation;
import com.alibaba.dubbo.performance.demo.agent.server.AgentConstant;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
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
 * Time: 下午3:20
 */
public class ProviderRpcClient {

    private Logger logger = LoggerFactory.getLogger(ProviderRpcClient.class);

    private Channel channel;

    public ProviderRpcClient(Channel sourceChannel) throws InterruptedException {

        Bootstrap bootstrap = new Bootstrap()
                .group(sourceChannel.eventLoop())
                .remoteAddress("127.0.0.1", AgentConstant.DUBBO_PORT)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .channel(EpollSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new DubboRpcEncoder());
                        pipeline.addLast(new DubboRpcDecoder());
                        pipeline.addLast(new ProviderRpcHandler(sourceChannel));
                    }
                });

        channel = bootstrap.connect().channel();

//        ChannelFuture f = bootstrap.connect("127.0.0.1", Integer.valueOf(System.getProperty("dubbo.protocol.port")));
//
//        f.addListener(future -> channel = ((ChannelFuture) future).channel());

    }


    public void write(Long requestId, String interfaceName, String method, String parameterTypesString, String parameter) throws Exception {

        RpcInvocation invocation = new RpcInvocation();
        invocation.setMethodName(method);
        invocation.setAttachment("path", interfaceName);
        invocation.setParameterTypes(parameterTypesString);    // Dubbo内部用"Ljava/lang/String"来表示参数类型是String

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
        JsonUtils.writeObject(parameter, writer);
        invocation.setArguments(out.toByteArray());

        Request request = new Request(requestId);
        request.setVersion("2.0.0");
        request.setTwoWay(true);
        request.setData(invocation);

        channel.writeAndFlush(request, channel.voidPromise());
    }

    public void deactivate() {
        channel.close();
    }

}
