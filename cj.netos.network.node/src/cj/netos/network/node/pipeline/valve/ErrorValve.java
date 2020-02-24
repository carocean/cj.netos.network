package cj.netos.network.node.pipeline.valve;

import cj.netos.network.*;
import cj.netos.network.node.*;
import cj.studio.ecm.net.CircuitException;
import io.netty.channel.Channel;

import java.util.ArrayList;

public class ErrorValve extends ChannelWriter implements IValve {
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
        CircuitException ce = CircuitException.search(error);

        NetworkFrame back = new NetworkFrame("error /system/notify network/1.0");

        IPrincipal principal = pipeline.principal();
        if (principal != null) {
            frame.head("sender-person", principal.principal());
            frame.head("sender-peer", principal.peer());
        }

        if (ce != null) {
            back.head("status", ce.getStatus());
            back.head("message", ce.getMessage().replace("\r", "").replace("\n", ""));
        } else {
            back.head("status", "500");
            back.head("message", (error.getMessage() + "").replace("\r", "").replace("\n", ""));
        }
        frame.head("error-source", frame.url());
        //不管有没有上下文就当成有
        String name=frame.rootName();
        if (networkContainer.containsNetwork(name)) {
            frame.head("send-network", name);
        }else{
            frame.head("send-network", "");
        }
        write((Channel) pipeline.attachment(), back);

        IEndpointer endpointer = null;
        if (pipeline == null) {
            endpointer=endpointerContainer.availableEndpoint();
        }else{
            endpointer = endpointerContainer.endpoint(principal.key());
        }
        if (endpointer != null) {
            NetworkFrame f = new NetworkFrame(String.format("error /%s/notify/ network/1.0", networkContainer.getEventNetwork()));
            f.head("error-source",frame.url());
            endpointer.upstream(principal,f);
        }

    }

}
