package cj.netos.network.node.pipeline.valve;

import cj.netos.network.*;
import cj.netos.network.node.Direction;
import cj.netos.network.node.IEndport;
import cj.netos.network.node.IEndportContainer;
import cj.netos.network.node.IPump;
import cj.netos.network.node.eventloop.EventTask;
import cj.studio.ecm.net.CircuitException;


public class UpstreamEndportValve implements IValve {
    private final IPump pump;
    private IEndportContainer endportContainer;

    public UpstreamEndportValve(INetworkServiceProvider site) {
        endportContainer = (IEndportContainer) site.getService("$.network.endportContainer");
        pump = (IPump) site.getService("$.network.pump");
    }

    @Override
    public void flow(NetworkFrame frame, IPipeline pipeline) throws CircuitException {
        IPrincipal principal = pipeline.principal();
        IEndport endport = endportContainer.openport(principal);
        endport.openUpstream().write( frame).close();
        EventTask task = new EventTask(Direction.upstream, principal.key(), frame.rootName());
        pump.arriveUpstream(task);
        pipeline.nextFlow(frame, this);
    }

    @Override
    public void nextError(NetworkFrame frame, Throwable error, IPipeline pipeline) throws CircuitException {
        pipeline.nextError(frame, error, this);
    }

}
