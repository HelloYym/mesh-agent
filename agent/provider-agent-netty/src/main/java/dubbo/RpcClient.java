package dubbo;

import dubbo.model.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class RpcClient {
    private Logger logger = LoggerFactory.getLogger(RpcClient.class);

    private Channel channel;

    public RpcClient(Channel sourceChannel) {

        Bootstrap bootstrap = new Bootstrap()
                .group(sourceChannel.eventLoop())
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new DubboRpcEncoder());
                        pipeline.addLast(new DubboRpcDecoder());
                        pipeline.addLast(new RpcClientHandler(sourceChannel));
                    }
                });

        String os = System.getProperty("os.name");
        if (os.equals("Mac OS X")) {
            bootstrap.channel(NioSocketChannel.class);
        } else {
            bootstrap.channel(EpollSocketChannel.class);
        }

        ChannelFuture f = bootstrap.connect("127.0.0.1", Integer.valueOf(System.getProperty("dubbo.protocol.port")));

        f.addListener(future -> channel = ((ChannelFuture) future).channel());
    }

    public ChannelFuture write(Long requestId, String interfaceName, String method, String parameterTypesString, String parameter) throws Exception {

        RpcInvocation invocation = new RpcInvocation();
        invocation.setMethodName(method);
        invocation.setAttachment("path", interfaceName);
        invocation.setParameterTypes(parameterTypesString);    // Dubbo内部用"Ljava/lang/String"来表示参数类型是String

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
        JsonUtils.writeObject(parameter, writer);
        invocation.setArguments(out.toByteArray());

        Request request = new Request();
        request.setId(requestId);
        request.setVersion("2.0.0");
        request.setTwoWay(true);
        request.setData(invocation);

        return channel.writeAndFlush(request);
    }
}
