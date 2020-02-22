package cj.netos.network.node;

import cj.netos.network.Castmode;
import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.IPrincipal;
import cj.netos.network.NetworkFrame;
import cj.studio.ecm.net.CircuitException;

public interface INetworkContainer {
    boolean isAutoCreate();

    String getEventNetwork();

    INetwork getNetwork(String network);

    void createNetwork(IPrincipal principal, String name, String title, Castmode frontendCastmode, Castmode backendCastmode) throws CircuitException;

    void removeNetwork(IPrincipal principal, String network) throws CircuitException;

    void viewNetwork(IPrincipal principal, String network) throws CircuitException;


    void listNetwork(IPrincipal principal) throws CircuitException;


    void joinNetwork(IPrincipal principal, String network, boolean joinToFrontend) throws CircuitException;

    void leaveNetwork(IPrincipal principal, String network, boolean joinToFrontend) throws CircuitException;

    void onerror(IPrincipal principal, NetworkFrame frame, Throwable error) throws CircuitException;

    void offline(IEndpointer endpointer) throws CircuitException;

    void online(IEndpointer endpointer) throws CircuitException;

    void onnofity(NetworkFrame frame, IPrincipal principal) throws CircuitException;

    void load(INetworkServiceProvider site, NetworkConfig config);

}
