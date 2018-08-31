import com.google.protobuf.Message;
import communication.MessageProtos;
import dubbo.RpcClient;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import registry.EtcdRegistry;
import registry.IRegistry;

import java.net.InetAddress;
import java.text.MessageFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by yangyuming on 2018/5/23.
 */
public class HashServerHandler extends ChannelInboundHandlerAdapter {

    private Logger logger = LoggerFactory.getLogger(HashServerHandler.class);

    private RpcClient rpcClient = null;

    private static AtomicLong activeId = new AtomicLong();


    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {

        MessageProtos.Request request = (MessageProtos.Request) msg;
         Long requestId = request.getRequestId();
        String interfaceName = request.getInterface();
        String method = request.getMethod();
        String parameterTypesString = request.getParameterTypesString();
        String parameter = request.getParameter();

        ChannelFuture future = rpcClient.write(requestId, interfaceName, method, parameterTypesString, parameter);

        ReferenceCountUtil.release(msg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Long id = activeId.incrementAndGet();
        logger.info(MessageFormat.format("ProviderAgentServerHandler channelActive: {0}", id.toString()));
        rpcClient = new RpcClient(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    //    模拟provider用于测试
    private int hash(String str) throws Exception {
        int hashCode = str.hashCode();
        sleep();
        return hashCode;
    }

    private void sleep() throws Exception {
        Thread.sleep((long) 50);
    }
}
