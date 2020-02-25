package cj.netos.network.peer;


import cj.netos.network.NetworkFrame;
import cj.studio.ecm.net.CircuitException;

public interface ILogicNetwork {
    String getNetwork();

    void leave() throws CircuitException;

    void ls(String memberIn) throws CircuitException;

    void send(NetworkFrame frame) throws CircuitException;

    void onmessage(IOnmessage onmessage);


}
