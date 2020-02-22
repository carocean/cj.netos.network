package cj.netos.network.peer;


import cj.netos.network.NetworkFrame;

public interface ILogicNetwork {
    String getNetworkName();

    void info();

    void send(NetworkFrame frame);

    void leave();
}
