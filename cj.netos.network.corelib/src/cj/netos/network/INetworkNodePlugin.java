package cj.netos.network;

import cj.studio.ecm.net.CircuitException;

public interface INetworkNodePlugin  {

    void onstart(String pluginHome, INetworkServiceProvider site) throws CircuitException;

    ICheckRights createCheckRights();
    void combine(IPipeline pipeline);

    void demolish(IPipeline pipeline);

}
