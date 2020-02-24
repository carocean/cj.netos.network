package cj.netos.network.node;

import cj.netos.network.*;
import cj.studio.ecm.net.CircuitException;

public interface INetworkContainer {
    boolean isAutoCreate();

    String getEventNetwork();

    INetwork getNetwork(String network);

    void createNetwork(IPrincipal principal, String name, String title, FrontendCastmode frontendCastmode, BackendCastmode backendCastmode) throws CircuitException;

    void removeNetwork(IPrincipal principal, String network) throws CircuitException;


    void joinNetwork(IPrincipal principal, String network, boolean joinToFrontend) throws CircuitException;

    void leaveNetwork(IPrincipal principal, String network) throws CircuitException;


    void load(INetworkServiceProvider site, NetworkConfig config);

    String[] enumNetwork();

    boolean containsNetwork(String name);

}
