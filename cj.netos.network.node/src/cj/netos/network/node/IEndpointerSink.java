package cj.netos.network.node;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.NetworkFrame;

public interface IEndpointerSink {
    String key();


    String getNetwork();

    void open(String key, String network, INetworkServiceProvider site);

    void close();

    NetworkFrame pullFirst();

    void removeFirst();

    void write(NetworkFrame frame);

}
