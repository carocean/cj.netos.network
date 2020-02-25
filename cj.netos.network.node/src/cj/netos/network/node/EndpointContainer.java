package cj.netos.network.node;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.IPrincipal;
import cj.studio.ecm.net.CircuitException;
import io.netty.channel.Channel;

public class EndpointContainer implements IEndpointerContainer {
    @Override
    public void offline(Channel channel) throws CircuitException {

    }

    @Override
    public void online(IPrincipal principal) {

    }

    @Override
    public IEndpointer createEndpointer(IPrincipal principal, Channel channel) throws CircuitException {
        return null;
    }

    @Override
    public IEndpointer endpointer(String endpointerKey) {
        return null;
    }

    @Override
    public boolean hasEndpointer(String endpointerKey) {
        return false;
    }

    public EndpointContainer(INetworkServiceProvider site) {

    }
}
