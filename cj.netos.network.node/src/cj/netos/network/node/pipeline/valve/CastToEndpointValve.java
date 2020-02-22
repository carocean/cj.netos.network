package cj.netos.network.node.pipeline.valve;

import cj.netos.network.*;
import cj.netos.network.node.IEndpointer;
import cj.netos.network.node.IEndpointerContainer;
import cj.studio.ecm.net.CircuitException;
import io.netty.channel.Channel;


public class CastToEndpointValve implements IValve {
    private IEndpointerContainer endpointerContainer;

    public CastToEndpointValve(INetworkServiceProvider site) {
        endpointerContainer = (IEndpointerContainer) site.getService("$.network.endpointerContainer");
    }

    @Override
    public void flow(NetworkFrame frame, IPipeline pipeline) throws CircuitException {
        IPrincipal principal = pipeline.principal();
        Channel channel = (Channel) pipeline.attachment();
        IEndpointer endpoint = endpointerContainer.openEndpoint(principal, channel);
        endpoint.upstream(frame);
        pipeline.nextFlow(frame,this);
    }

    @Override
    public void nextError(NetworkFrame frame, Throwable error, IPipeline pipeline) throws CircuitException {
        pipeline.nextError(frame,error,this);
    }

}
