package cj.netos.network.node;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.IPrincipal;
import cj.netos.network.NetworkFrame;
import io.netty.channel.Channel;

import java.util.Map;

public interface IEndpointer {
    ChannelWriter getChannelWriter();

    Channel getChannel();

    String key();

    void upstream(IPrincipal principal, NetworkFrame frame);

    void close();

    IPrincipal getPrincipal();

    Map<String, IEndpointerSink> getSinks();

    void open(IPrincipal principal, Channel channel, INetworkServiceProvider site);

    void joinNetwork(String network);

    void leaveNetwork(String network);

    boolean downstream(NetworkFrame frame, String fromNetwork);

}
