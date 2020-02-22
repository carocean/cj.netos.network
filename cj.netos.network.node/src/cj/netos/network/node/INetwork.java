package cj.netos.network.node;

import cj.netos.network.Castmode;
import cj.netos.network.IPrincipal;
import cj.netos.network.NetworkFrame;
import cj.studio.ecm.net.CircuitException;

import java.util.Set;

public interface INetwork {

    void cast(NetworkFrame frame) throws CircuitException;


    INetworkSink getFrontendSink(String endpointKey);

    Set<String> enumFrontendSinkKey();

    INetworkSink getBackendSink(String endpointKey);

    Set<String> enumBackendSinkKey();

    String getName();

    String getTitle();

    Castmode getFrontendCastmode();

    Castmode getBackendCastmode();

    INetworkSink getFrontendSink(IPrincipal principal);


    INetworkSink getBackendSink(IPrincipal principal);


    void join(IPrincipal principal, boolean joinToFrontend);

    void leave(IPrincipal principal, boolean isLeaveFrontend);

}
