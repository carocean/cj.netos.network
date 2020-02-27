package cj.netos.network.node;

import cj.netos.network.IPrincipal;
import cj.netos.network.NetworkFrame;
import io.netty.channel.Channel;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DefaultEndpointer implements IEndpointer {
    Channel channel;
    IPrincipal principal;
    ChannelWriter writer;
    List<String> listenNetworks;

    public DefaultEndpointer(Channel channel, IPrincipal principal) {
        this.channel = channel;
        this.principal = principal;
        writer = new ChannelWriter();
        this.listenNetworks = new CopyOnWriteArrayList<>();
    }

    @Override
    public void write(NetworkFrame frame) {
        writer.write(channel, frame);
    }

    @Override
    public void close() {
        if (channel!=null&&channel.isOpen()) {
            channel.close();
        }
        channel = null;
        writer = null;

    }

    @Override
    public String[] listenNetworks() {
        return listenNetworks.toArray(new String[0]);
    }

    @Override
    public void removeListenNetwork(String network) {
        listenNetworks.remove(network);
    }

    @Override
    public void addListenNetwork(String networkName) {
        if (listenNetworks.contains(networkName)) {
            return;
        }
        listenNetworks.add(networkName);
    }

    @Override
    public boolean hasListenNetwork(String network) {
        return listenNetworks.contains(network);
    }
}
