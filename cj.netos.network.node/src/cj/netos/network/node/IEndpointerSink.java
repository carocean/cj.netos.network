package cj.netos.network.node;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.NetworkFrame;
import cj.netos.network.TransferMode;
import org.apache.jdbm.DB;

public interface IEndpointerSink {
    String key();

    TransferMode getMode();

    String getNetwork();

    void open(String key, String network, TransferMode mode, INetworkServiceProvider site);

    void close();

    NetworkFrame pullFirst();

    void removeFirst();

    void write(NetworkFrame frame);

}
