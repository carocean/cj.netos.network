package cj.netos.network.node;

import cj.netos.network.*;
import cj.studio.ecm.net.CircuitException;

import java.util.Set;

public interface INetwork {

    void cast(Sender sender,NetworkFrame frame) throws CircuitException;

    INetworkSink getFrontendSink(String endpointKey);

    Set<String> enumFrontendSinkKey();

    INetworkSink getBackendSink(String endpointKey);

    Set<String> enumBackendSinkKey();

    String getName();

    String getTitle();

    FrontendCastmode getFrontendCastmode();

    BackendCastmode getBackendCastmode();

    INetworkSink getFrontendSink(IPrincipal principal);


    INetworkSink getBackendSink(IPrincipal principal);


    void join(IPrincipal principal, boolean joinToFrontend);

    void leave(IPrincipal principal);

    void close();

}
