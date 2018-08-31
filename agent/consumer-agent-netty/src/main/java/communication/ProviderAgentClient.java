package communication;

import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import registry.Endpoint;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import registry.IRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: yangyuming
 * Date: 2018/5/24
 * Time: 下午9:54
 */

public class ProviderAgentClient {

    private Logger logger = LoggerFactory.getLogger(ProviderAgentClient.class);

    private Bootstrap bootstrap;

    private Random random = new Random();

    private List<Channel> providerChannelList = new ArrayList<>();

    public ProviderAgentClient(IRegistry registry) throws Exception {

        this.bootstrap = new Bootstrap();


        String os = System.getProperty("os.name");
        if (os.equals("Mac OS X")) {
            bootstrap.group(new NioEventLoopGroup());
            bootstrap.channel(NioSocketChannel.class);
        } else {
            bootstrap.group(new EpollEventLoopGroup());
            bootstrap.channel(EpollSocketChannel.class);
        }


        bootstrap.option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();

                        // Decoders
                        pipeline.addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
                        pipeline.addLast("protobufDecoder", new ProtobufDecoder(MessageProtos.Response.getDefaultInstance()));
                        pipeline.addLast(new ProviderAgentClientHandler());

                        // Encoder
                        pipeline.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
                        pipeline.addLast("protobufEncoder", new ProtobufEncoder());
                    }
                });

        List<Endpoint> providerEndpointList = registry.find("com.alibaba.dubbo.performance.demo.provider.IHelloService");

        logger.info(String.valueOf(providerEndpointList.size()));

        for (Endpoint endpoint : providerEndpointList) {
            int weight = endpoint.getWeight();


            ChannelFuture f = bootstrap.connect(endpoint.getHost(), endpoint.getPort());
            f.addListener(future -> {
                Channel channel = ((ChannelFuture) future).channel();
                for (int i = 0; i < weight; i++)
                    providerChannelList.add(channel);
            });

//            for (int i = 0; i < weight; i++) {
//                ChannelFuture f = bootstrap.connect(endpoint.getHost(), endpoint.getPort());
//                f.addListener(future -> {
//                    Channel channel = ((ChannelFuture) future).channel();
//                    providerChannelList.add(channel);
//                });
//            }
        }
    }


    public Channel getChannel() throws Exception {
        return providerChannelList.get(random.nextInt(providerChannelList.size()));
    }

}