package cj.netos.network.peer;


import cj.netos.network.NetworkFrame;
import cj.studio.ecm.net.CircuitException;

public interface IOnmessage {
    void onmessage(ILogicNetwork logicNetwork, NetworkFrame frame) throws CircuitException;
}
