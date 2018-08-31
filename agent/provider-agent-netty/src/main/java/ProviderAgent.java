import communication.MessageProtos;
import dubbo.RpcClient;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.codec.string.LineEncoder;
import io.netty.handler.codec.string.LineSeparator;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import registry.EtcdRegistry;
import registry.IRegistry;

/**
 * Created by IntelliJ IDEA.
 * User: yangyuming
 * Date: 2018/5/23
 * Time: 下午9:54
 */

public class ProviderAgent {

    private int port;
    private IRegistry registry;
    private RpcClient rpcClient;
    private String os;

    public ProviderAgent(int port) {
        this.port = port;
        this.os = System.getProperty("os.name");
        registry = new EtcdRegistry(System.getProperty("etcd.url"));
    }

    private void run() throws Exception {

        ServerBootstrap b = new ServerBootstrap();

        if (os.equals("Mac OS X")) {
            b.group(new NioEventLoopGroup(), new NioEventLoopGroup());
            b.channel(NioServerSocketChannel.class);
        } else {
            b.group(new EpollEventLoopGroup(), new EpollEventLoopGroup());
            b.channel(EpollServerSocketChannel.class);
        }

        b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childHandler(new ChannelInitializer<SocketChannel>() { // (4)

                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        // Decoders
                        pipeline.addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
                        pipeline.addLast("protobufDecoder", new ProtobufDecoder(MessageProtos.Request.getDefaultInstance()));

                        // Encoder
                        pipeline.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
                        pipeline.addLast("protobufEncoder", new ProtobufEncoder());

                        pipeline.addLast(new HashServerHandler());
                    }
                });

        try {
            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync();
            f.channel().closeFuture().sync();
        } finally {
            b.config().group().shutdownGracefully();
            b.config().childGroup().shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = Integer.valueOf(System.getProperty("server.port"));
        new ProviderAgent(port).run();
    }

}
