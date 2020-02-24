package cj.netos.network.peer;


import cj.netos.network.NetworkFrame;

public interface IOnmessage {
    void onmessage(ILogicNetwork logicNetwork,NetworkFrame frame);
}
