package cj.netos.network.node;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.NetworkFrame;

/**
 * 表示network中对应终结点的东西，即槽
 */
public interface INetworkSink {
    String getOwnerNetwork();

    void write(NetworkFrame frame);
    NetworkFrame pullFirst();
    void removeFirst();
    String getKey();

    void open(INetworkServiceProvider site, String network, String principal, String peer);

    void close();

}
