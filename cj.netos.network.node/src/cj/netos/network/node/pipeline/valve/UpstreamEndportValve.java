package cj.netos.network.node.pipeline.valve;

import cj.netos.network.*;
import cj.netos.network.node.*;
import cj.netos.network.node.eventloop.EventTask;
import cj.studio.ecm.net.CircuitException;


public class UpstreamEndportValve implements IValve {
    private final IPump pump;
    private IEndportContainer endportContainer;
    INetworkContainer networkContainer;

    public UpstreamEndportValve(INetworkServiceProvider site) {
        endportContainer = (IEndportContainer) site.getService("$.network.endportContainer");
        networkContainer = (INetworkContainer) site.getService("$.network.networkContainer");
        pump = (IPump) site.getService("$.network.pump");
    }

    @Override
    public void flow(NetworkFrame frame, IPipeline pipeline) throws CircuitException {
        String network = frame.rootName();
        if (!networkContainer.containsNetwork(network)) {
            throw new CircuitException("404", "请求的网络不存在:" + network);
        }
        IPrincipal principal = pipeline.principal();
        IEndport endport = endportContainer.openport(principal);
        endport.openUpstream(network).write(frame).close();
        EventTask task = new EventTask(Direction.upstream, principal.key(),network);
        pump.arriveUpstream(task);
        pipeline.nextFlow(frame, this);
    }

    @Override
    public void nextError(NetworkFrame frame, Throwable error, IPipeline pipeline) throws CircuitException {
        pipeline.nextError(frame, error, this);
    }

}
