package server;

import com.google.protobuf.Message;
import communication.MessageProtos;
import communication.ProviderAgentClient;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: chenyifan
 * Date: 2018-05-23
 * Time: 下午3:49
 */

@Sharable
public class ConsumerAgentHttpServerHandler extends ChannelInboundHandlerAdapter {

    private Logger logger = LoggerFactory.getLogger(ConsumerAgentHttpServerHandler.class);

    private static AtomicLong requestId = new AtomicLong();

    private ProviderAgentClient client;

    private Channel targetChannel;

//    private static AtomicLong constructorId = new AtomicLong();
//    private static AtomicLong activeId = new AtomicLong();

    ConsumerAgentHttpServerHandler(ProviderAgentClient client) {
        this.client = client;
//        Long id = constructorId.incrementAndGet();
//        if (id % 100 == 0)
//            logger.info(MessageFormat.format("ConsumerAgentHttpServerHandler Constuctor: {0}", id.toString()));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        Map<String, String> pMap = parse((FullHttpRequest) msg);

        Long id = requestId.incrementAndGet();
        ConsumerAgentServer.channelMap.put(id, ctx.channel());

        String interfaceName = pMap.get("interface");
        String parameterTypesString = pMap.get("parameterTypesString");
        String method = pMap.get("method");
        String parameter = pMap.getOrDefault("parameter", "");

        Message request = MessageProtos.Request.newBuilder()
                .setRequestId(id)
                .setInterface(interfaceName)
                .setParameterTypesString(parameterTypesString)
                .setMethod(method)
                .setParameter(parameter)
                .build();

//        targetChannel = client.getChannel();
        targetChannel.writeAndFlush(request);

        ReferenceCountUtil.release(msg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        targetChannel = client.getChannel();

//        Long id = activeId.incrementAndGet();
//        if (id % 100 == 0)
//            logger.info(MessageFormat.format("ConsumerAgentServerHandler channelActive: {0}", id.toString()));
    }


    private Map<String, String> parse(FullHttpRequest fullReq) throws IOException {

        Map<String, String> parmMap = new HashMap<>();

        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(fullReq);
        decoder.offer(fullReq);

        List<InterfaceHttpData> parmList = decoder.getBodyHttpDatas();

        for (InterfaceHttpData parm : parmList) {
            Attribute data = (Attribute) parm;
            parmMap.put(data.getName(), data.getValue());
        }
        return parmMap;
    }

}
