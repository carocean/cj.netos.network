package cj.netos.network.peer;

import cj.netos.network.NetworkFrame;
import cj.studio.ecm.net.CircuitException;

public class DefaultLogicNetwork implements ILogicNetwork {
    private IConnection connection;
    String network;
    private IOnmessage onmessage;


    public DefaultLogicNetwork(String networkName, IConnection connection) {
        this.network = networkName;
        this.connection = connection;
    }

    @Override
    public void leave() throws CircuitException {
        NetworkFrame frame = new NetworkFrame(String.format("leaveNetwork /%s network/1.0", network));
        connection.send(frame);
        connection.removeLogicNetwork(network);
        connection = null;
    }

    @Override
    public void ls() throws CircuitException {
        NetworkFrame frame = new NetworkFrame(String.format("viewNetwork /%s network/1.0", network));
        connection.send(frame);
    }

    @Override
    public void send(NetworkFrame frame) throws CircuitException {
        String old = frame.url();
        String url = String.format("/%s%s", network, old);
        frame.url(url);
        connection.send(frame);
        frame.url(old);
    }

    public void fireOnmessage(NetworkFrame frame) {
        if (onmessage != null) {
            String old = frame.url();
            String url = frame.relativeUrl();
            frame.url(url);
            onmessage.onmessage(this,frame);
            frame.url(old);
        }
    }

    @Override
    public String getNetwork() {
        return network;
    }

    @Override
    public void onmessage(IOnmessage onmessage) {
        this.onmessage = onmessage;
    }

}
