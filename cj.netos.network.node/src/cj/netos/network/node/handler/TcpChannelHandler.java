package cj.netos.network.node.handler;

import cj.netos.network.*;
import cj.netos.network.node.IEndpointerContainer;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.logging.ILogging;
import cj.studio.ecm.net.util.TcpFrameBox;
import cj.ultimate.util.StringUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

//使用reactor接收消息
//推送系统仅推送简单本文，对于较大的多媒体文件的推送不在本方案之内，可由客户端直接向文件服务上传而后通过推送系统将地址告诉另一方，另一方自动下载
public class TcpChannelHandler extends ChannelHandlerAdapter {
    public static ILogging logger;
    private IEndpointerContainer endpointerContainer;
    private IPipelineCombination combination;
    private IPipeline pipeline;

    public TcpChannelHandler(INetworkServiceProvider parent) {
        logger = CJSystem.logging();
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
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //如果心跳则退出，如果是空消息则退出，如果不是frame格式则退出
        //从container中找key，将消息放入reactor
        ByteBuf bb = (ByteBuf) msg;
        if (bb.readableBytes() == 0) {
            return;
        }
        byte[] b = new byte[bb.readableBytes()];
        bb.readBytes(b);
        bb.release();//系统会释放
        if (b.length < 1) {
            return;
        }
        PackFrame pack = new PackFrame(b);
        if (pack.isInvalid()) {
            return;
        }
        if (pack.isHeartbeat()) {
            feedbackHeartbeat(ctx);
//            CJSystem.logging().info(getClass(),"收到心跳包");
            return;
        }
        NetworkFrame frame = pack.getFrame();
        if (frame == null) {
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

    private void feedbackHeartbeat(ChannelHandlerContext ctx) {
        NetworkFrame f = new NetworkFrame("heartbeat / network/1.0");
        PackFrame pack = new PackFrame((byte) 2, f);
        byte[] box = TcpFrameBox.box(pack.toBytes());
        pack.dispose();
        ByteBuf bb = Unpooled.buffer();
        bb.writeBytes(box, 0, box.length);
        ctx.channel().writeAndFlush(bb);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        ctx.channel().attr(AttributeKey.valueOf("Net-Protocol")).set("tcp");
        pipeline.attachment(ctx.channel());
        combination.combine(pipeline);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
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
