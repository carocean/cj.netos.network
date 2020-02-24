package cj.netos.network.peer;

import cj.netos.network.*;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.EcmException;
import cj.studio.ecm.net.CircuitException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;

class WSClientHandler extends SimpleChannelInboundHandler<Object> {

    INetworkServiceProvider site;
    IPipeline pipeline;
    WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;

    public WSClientHandler(WebSocketClientHandshaker handshaker, INetworkServiceProvider site) {
        this.handshaker = handshaker;
        this.site=site;
    }


    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        pipeline = new DefaultPipeline(site);
        IPipelineCombination combination = (IPipelineCombination) site.getService("$.pipelineCombination");
        combination.combine(pipeline);
        IConnection connection =(IConnection) site.getService("$.connection");
        connection.onopen();

        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        //断开重连

        long heartbeat = (long) site.getService("$.prop.heartbeat");
        long reconnecttimes = (long) site.getService("$.prop.reconnect_times");
        long reconnectinterval = (long) site.getService("$.prop.reconnect_interval");
        IConnection connection =(IConnection) site.getService("$.connection");
        if (heartbeat > 0 && !connection.isForbiddenReconnect()) {
            boolean succeed = false;
            for (long i = 0; reconnecttimes > 0 ? (i < reconnecttimes) : true; i++) {//重试次数
                try {
                    connection.reconnect();
                    CJSystem.logging().info(getClass(), "重新连接成功");
                    succeed = true;
                    break;
                } catch (Throwable throwable) {
                    CJSystem.logging().warn(getClass(), throwable.getMessage());
                    if (reconnectinterval > 0) {
                        Thread.sleep(reconnectinterval);//隔多少秒后重连
                    }
                    continue;
                }
            }
            if (!succeed) {
                IPipelineCombination combination = (IPipelineCombination) site.getService("$.pipelineCombination");
                combination.demolish(pipeline);
                connection.onclose();
            }
            return;
        }
        IPipelineCombination combination = (IPipelineCombination) site.getService("$.pipelineCombination");
        combination.demolish(pipeline);
        pipeline.dispose();
        pipeline = null;
        connection.onclose();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            // 不管是读事件空闲还是写事件空闲都向服务器发送心跳包
            sendHeartbeatPacket(ctx);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    private void sendHeartbeatPacket(ChannelHandlerContext ctx) throws CircuitException {
        WebSocketFrame frame = new PingWebSocketFrame(Unpooled.wrappedBuffer(new byte[] { 8, 1, 8, 1 }));
        ctx.channel().writeAndFlush(frame);
//        CJSystem.logging().debug(getClass(),"发送心跳包");
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            handshaker.finishHandshake(ch, (FullHttpResponse) msg);
            System.out.println("WebSocket Client connected!");
            handshakeFuture.setSuccess();
            return;
        }

        if (msg instanceof PongWebSocketFrame) {
//            CJSystem.logging().debug(getClass(),"收到心跳包");
            return;
        }
        if (msg instanceof CloseWebSocketFrame) {
            ch.close();
            return;
        }
        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new Exception("Unexpected FullHttpResponse (getStatus=" + response.status() + ", content="
                    + response.content().toString(CharsetUtil.UTF_8) + ')');
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
        pipeline.input(frame);
    }
}