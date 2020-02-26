package cj.netos.network.node;

import cj.netos.network.NetworkFrame;

public interface IEndpointer {
    void write(NetworkFrame frame);

    void close();

    boolean hasListenNetwork(String network);

    void addListenNetwork(String network);

    void removeListenNetwork(String network);

    String[] listenNetworks();

}
