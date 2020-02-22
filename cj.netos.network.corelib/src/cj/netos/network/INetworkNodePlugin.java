package cj.netos.network;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.IPrincipal;
import cj.netos.network.IValve;
import cj.studio.ecm.net.CircuitException;

public interface INetworkNodePlugin  {

    void onstart(String pluginHome, INetworkServiceProvider site) throws CircuitException;

    ICheckRights createCheckRights();
    void combine(IPipeline pipeline);

    void demolish(IPipeline pipeline);

}
