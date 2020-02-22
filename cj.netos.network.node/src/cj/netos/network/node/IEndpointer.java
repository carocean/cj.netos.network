package cj.netos.network.node;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.IPrincipal;
import cj.netos.network.NetworkFrame;
import cj.netos.network.TransferMode;
import io.netty.channel.Channel;
import org.apache.jdbm.DB;

import java.util.Map;

public interface IEndpointer {
    String key();

    void upstream(NetworkFrame frame);

    void close();

    IPrincipal getPrincipal();

    Map<String, IEndpointerSink> getSinks();

    void open(IPrincipal principal, Channel channel, INetworkServiceProvider site);

    void joinNetwork(String network, TransferMode mode);

    void leaveNetwork(String network);

    boolean downstream(NetworkFrame frame, String fromNetwork);

}
