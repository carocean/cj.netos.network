package cj.netos.network.node;

import cj.netos.network.NetworkFrame;
import cj.netos.network.PackFrame;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.net.util.TcpFrameBox;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class ChannelWriter {
    public void write(Channel channel, NetworkFrame frame) {
        AttributeKey<String> key = AttributeKey.valueOf("Net-Protocol");
        String protocol = channel.attr(key).get();
        switch (protocol) {
            case "tcp":
                PackFrame pack = new PackFrame((byte) 1, frame);
                byte[] box = TcpFrameBox.box(pack.toBytes());
                pack.dispose();
                ByteBuf bb = Unpooled.buffer();
                bb.writeBytes(box, 0, box.length);
                if (!channel.isWritable()) {
                    CJSystem.logging().warn(ChannelWriter.class, "套节字不能定");
                    break;
                }
                try {
                    channel.writeAndFlush(bb);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    break;
                }
                break;
            case "websocket":
                BinaryWebSocketFrame binaryWebSocketFrame = new BinaryWebSocketFrame();
                binaryWebSocketFrame.content().writeBytes(frame.toByteBuf());
                channel.writeAndFlush(binaryWebSocketFrame);
                break;
        }
    }
}
