package server;

import communication.ProviderAgentClient;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import registry.EtcdRegistry;
import registry.IRegistry;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by IntelliJ IDEA.
 * User: yangyuming
 * Date: 2018/5/23
 * Time: 下午9:53
 */

public class ConsumerAgentServer {

    private int port;
    private IRegistry registry;
    private String os;

    public static ConcurrentHashMap<Long, Channel> channelMap = new ConcurrentHashMap<>();

    private ConsumerAgentServer(int port) {
        this.port = port;
        this.os = System.getProperty("os.name");
        registry = new EtcdRegistry(System.getProperty("etcd.url"));
    }

    private void run() throws Exception {

        ServerBootstrap bootstrap = new ServerBootstrap();

        if (os.equals("Mac OS X")) {
            bootstrap.group(new NioEventLoopGroup(), new NioEventLoopGroup());
            bootstrap.channel(NioServerSocketChannel.class);
        } else {
            bootstrap.group(new EpollEventLoopGroup(), new EpollEventLoopGroup());
            bootstrap.channel(EpollServerSocketChannel.class);
        }

        ProviderAgentClient providerAgentClient = new ProviderAgentClient(registry);
//        ConsumerAgentHttpServerHandler consumerAgentServerHandler = new ConsumerAgentHttpServerHandler(providerAgentClient);

        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new HttpServerCodec());
                        ch.pipeline().addLast(new HttpObjectAggregator(65536));
                        ch.pipeline().addLast(new ConsumerAgentHttpServerHandler(providerAgentClient));
                    }
                });


        try {
            ChannelFuture future = bootstrap.bind(port);
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bootstrap.config().group().shutdownGracefully();
            bootstrap.config().childGroup().shutdownGracefully();
        }
    }


    public static void main(String[] args) throws Exception {
        int port = Integer.valueOf(System.getProperty("server.port"));
        new ConsumerAgentServer(port).run();
    }

}
