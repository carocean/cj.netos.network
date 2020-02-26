package cj.netos.network.node;

import cj.netos.network.BackendCastmode;
import cj.netos.network.FrontendCastmode;
import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.IPrincipal;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.IClosable;

public interface INetworkContainer extends IClosable {
    boolean isAutoCreate();

    INetwork openNetwork(String network);

    void createNetwork(IPrincipal principal, String name, String title, FrontendCastmode frontendCastmode, BackendCastmode backendCastmode) throws CircuitException;


    void removeNetwork(String network) throws CircuitException;



    void joinNetwork(String network, IPrincipal principal, boolean joinToFrontend) throws CircuitException;

    void leaveNetwork(String network, IPrincipal principal) throws CircuitException;

    void load(INetworkServiceProvider site, NetworkConfig config, String home);

    String[] enumNetwork();

    boolean containsNetwork(String name);
}
