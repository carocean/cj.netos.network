package cj.netos.network.node.pipeline.valve;

import cj.netos.network.*;
import cj.netos.network.node.IEndpointer;
import cj.netos.network.node.IEndpointerContainer;
import cj.netos.network.node.INetworkContainer;
import cj.studio.ecm.net.CircuitException;
import io.netty.channel.Channel;

public class ErrorValve implements IValve {
    private final IEndpointerContainer endpointerContainer;
    private final INetworkContainer networkContainer;

    public ErrorValve(INetworkServiceProvider site) {
        endpointerContainer = (IEndpointerContainer) site.getService("$.network.endpointerContainer");
        networkContainer = (INetworkContainer) site.getService("$.network.networkContainer");
    }

    @Override
    public void flow(NetworkFrame frame, IPipeline pipeline) throws CircuitException {
        pipeline.nextFlow(frame, this);
    }

    @Override
    public void nextError(NetworkFrame frame, Throwable error, IPipeline pipeline) throws CircuitException {
        error.printStackTrace();

    }

}
