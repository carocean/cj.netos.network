package cj.netos.network.node;

import cj.netos.network.NetworkFrame;

public interface IEndpointer {
    void write(NetworkFrame frame);

    void close();

}
