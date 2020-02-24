
package cj.netos.network.node.initializer;

import cj.netos.network.node.handler.TcpChannelHandler;
import cj.netos.network.INetworkServiceProvider;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;


public class TcpChannelInitializer extends ChannelInitializer<SocketChannel> {
    INetworkServiceProvider parent;
    long heartbeat;

    public TcpChannelInitializer(INetworkServiceProvider parent) {
        this.parent=parent;
        this.heartbeat = (long) parent.getService("$.server.heartbeat");
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new LengthFieldBasedFrameDecoder(81920, 0, 4, 0, 4));
        if (heartbeat > 0) {
            pipeline.addLast(new IdleStateHandler(heartbeat, 0, 0, TimeUnit.SECONDS));
        }
        pipeline.addLast(new TcpChannelHandler(parent));
    }

}