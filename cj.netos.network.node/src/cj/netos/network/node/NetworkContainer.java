package cj.netos.network.node;

import cj.netos.network.*;
import cj.studio.ecm.EcmException;
import cj.studio.ecm.net.CircuitException;

import java.util.HashMap;
import java.util.Map;

public class NetworkContainer implements INetworkContainer {
    NetworkConfig config;
    Map<String, INetwork> networks;
    private INetworkServiceProvider site;

    @Override
    public void load(INetworkServiceProvider site, NetworkConfig config) {
        this.config = config;
        this.site = site;
        this.networks = new HashMap<>();
        for (String key : config.getNetworks().keySet()) {
            NetworkInfo networkInfo = config.getNetworks().get(key);
            INetwork network = new DefaultNetwork(site, networkInfo);
            if (key.equals(config.getEventNetwork())) {
                if (network.getFrontendCastmode() == FrontendCastmode.selectcast) {
                    throw new EcmException("事件中心不支持selectcast分发模式");
                }
            }
            networks.put(key, network);
        }
    }

    @Override
    public boolean isAutoCreate() {
        return config.isAutoCreate();
    }

    @Override
    public String getEventNetwork() {
        return config.getEventNetwork();
    }

    @Override
    public INetwork getNetwork(String network) {
        return networks.get(network);
    }


    @Override
    public void createNetwork(IPrincipal principal, String name, String title, FrontendCastmode frontendCastmode, BackendCastmode backendCastmode) throws CircuitException {
        if (networks.containsKey(name)) {
            throw new EcmException("已存在网络:" + name);
        }
        INetwork nk = new DefaultNetwork(site, name, title, frontendCastmode, backendCastmode);
        networks.put(name, nk);
    }


    @Override
    public void removeNetwork(IPrincipal principal, String network) throws CircuitException {
        INetwork nw = networks.get(network);
        if (nw != null) {
            nw.close();
        }
        networks.remove(network);
    }

    @Override
    public String[] enumNetwork() {
        return networks.keySet().toArray(new String[0]);
    }

    @Override
    public void joinNetwork(IPrincipal principal, String network, boolean joinToFrontend) throws CircuitException {
        INetwork nw = networks.get(network);
        if (nw == null) {
            return;
        }
        nw.join(principal, joinToFrontend);
    }

    @Override
    public void leaveNetwork(IPrincipal principal, String network) throws CircuitException {
        INetwork nw = networks.get(network);
        if (nw == null) {
            return;
        }
        nw.leave(principal);
    }

    @Override
    public boolean containsNetwork(String name) {
        return networks.containsKey(name);
    }
}
