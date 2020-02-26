package cj.netos.network.node.handler;

import cj.netos.network.*;
import cj.netos.network.node.IEndpointerContainer;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.EcmException;
import cj.studio.ecm.logging.ILogging;
import cj.ultimate.util.StringUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

//使用reactor接收消息
//推送系统仅推送简单本文，对于较大的多媒体文件的推送不在本方案之内，可由客户端直接向文件服务上传而后通过推送系统将地址告诉另一方，另一方自动下载
public class WSChannelHandler extends SimpleChannelInboundHandler<Object> {
    public static ILogging logger;
    private long overtimes;
    // 心跳丢失计数器
    private long counter;
    private IEndpointerContainer endpointerContainer;
    private IPipeline pipeline;
    private final IPipelineCombination combination;

    public WSChannelHandler(INetworkServiceProvider parent) {
        logger = CJSystem.logging();
        this.overtimes = (long) parent.getService("$.server.overtimes");
        endpointerContainer = (IEndpointerContainer) parent.getService("$.network.endpointerContainer");
        combination = (IPipelineCombination) parent.getService("$.server.pipeline.combination");
        pipeline = new DefaultPipeline(parent);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (!(evt instanceof IdleStateEvent)) {
            super.userEventTriggered(ctx, evt);
            return;
        }
        String client = "";
        AttributeKey<String> key = AttributeKey.valueOf("Peer-Name");
        Attribute<String> attribute = ctx.channel().attr(key);
        if (attribute != null && !StringUtil.isEmpty(attribute.get())) {
            client = attribute.get();
        } else {
            client = ctx.channel().remoteAddress().toString();
        }
        // 空闲6s之后触发 (心跳包丢失)
        if (overtimes > 0 && counter >= overtimes) {
            // 连续丢失10个心跳包 (断开连接)
            ctx.channel().close().sync();
            CJSystem.logging().warn(getClass(), String.format("客户端：%s，连续丢失了%s个心跳包 ,服务器主动断开与它的连接.", client, counter));
        } else {
            counter++;
            CJSystem.logging().warn(getClass(), String.format("客户端：%s，已丢失了%s个心跳包.", client, counter));
        }

    }
    //netty的坑，必须将counter放到channelRead方法，messageReceived会漏掉
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        counter = 0;
        super.channelRead(ctx, msg);
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof PingWebSocketFrame) {
            ((WebSocketFrame) msg).content().retain();
            ctx.channel().writeAndFlush(new PongWebSocketFrame(((WebSocketFrame) msg).content()));
//            CJSystem.logging().debug(getClass(),"发送心跳包");
            return;
        }
        if (msg instanceof PongWebSocketFrame) {
            // Pong frames need to get ignored
            return;
        }
        if (msg instanceof CloseWebSocketFrame) {
            ctx.close();
            return;
        }
        ByteBuf bb = null;
        if (msg instanceof TextWebSocketFrame) {
            TextWebSocketFrame f = (TextWebSocketFrame) msg;
            bb = f.content();
        } else if (msg instanceof BinaryWebSocketFrame) {
            BinaryWebSocketFrame f = (BinaryWebSocketFrame) msg;
            bb = f.content();
        } else {
            throw new EcmException("不支持此类消息：" + msg.getClass());
        }
        if (bb.readableBytes() == 0) {
            return;
        }
        byte[] b = new byte[bb.readableBytes()];
        bb.readBytes(b);

        NetworkFrame frame = new NetworkFrame(b);
        if (frame == null || frame.isInvalid()) {
            return;
        }

        //以下路由到所请求的通道
        try {
            pipeline.input(frame);
        } catch (Throwable e) {
            pipeline.error(frame, e);
            logger.error(getClass(), e);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        ctx.channel().attr(AttributeKey.valueOf("Net-Protocol")).set("websocket");
        pipeline.attachment(ctx.channel());
        combination.combine(pipeline);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        counter = 0;
        endpointerContainer.offline(ctx.channel());
        combination.demolish(pipeline);
        pipeline.attachment(null);
        pipeline .dispose();
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        CJSystem.logging().error(getClass(), cause);
    }


}
