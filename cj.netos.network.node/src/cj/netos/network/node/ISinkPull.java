package cj.netos.network.node;

import cj.netos.network.NetworkFrame;

public interface ISinkPull {
    NetworkFrame pullFirst();

    void removeFirst();

}
