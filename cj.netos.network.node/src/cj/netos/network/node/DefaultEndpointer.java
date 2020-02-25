package cj.netos.network.node;

import cj.netos.network.IPrincipal;
import cj.netos.network.NetworkFrame;
import io.netty.channel.Channel;

public class DefaultEndpointer implements IEndpointer {
    Channel channel;
    IPrincipal principal;
    ChannelWriter writer;

    public DefaultEndpointer(Channel channel, IPrincipal principal) {
        this.channel = channel;
        this.principal = principal;
        writer = new ChannelWriter();
    }

    @Override
    public void write(NetworkFrame frame) {
        writer.write(channel, frame);
    }

    @Override
    public void close() {
        if (channel.isOpen()) {
            channel.close();
        }
        channel = null;
        writer = null;

    }
}
