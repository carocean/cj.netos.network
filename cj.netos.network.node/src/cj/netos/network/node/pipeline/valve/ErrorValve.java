package cj.netos.network.node.pipeline.valve;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.IPipeline;
import cj.netos.network.IValve;
import cj.netos.network.NetworkFrame;
import cj.netos.network.node.INetworkContainer;
import cj.studio.ecm.net.CircuitException;

public class ErrorValve implements IValve {
    INetworkContainer networkContainer;

    public ErrorValve(INetworkServiceProvider site) {
        networkContainer = (INetworkContainer) site.getService("$.network.networkContainer");
    }

    @Override
    public void flow(NetworkFrame frame, IPipeline pipeline) throws CircuitException {
        pipeline.nextFlow(frame, this);
    }

    @Override
    public void nextError(NetworkFrame frame, Throwable error, IPipeline pipeline) throws CircuitException {
        networkContainer.onerror(pipeline.principal(),frame, error);
    }

}
