package cj.netos.network.node;

import cj.netos.network.INetworkNodePlugin;
import cj.netos.network.INetworkServiceProvider;

public interface INetworkNodePluginLoader {
    INetworkNodePlugin scanAssemblyAndLoad(INetworkNodeConfig config, INetworkServiceProvider parent);

    void stopAssembly();
}
