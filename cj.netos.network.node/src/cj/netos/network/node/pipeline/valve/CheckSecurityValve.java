package cj.netos.network.node.pipeline.valve;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.IPipeline;
import cj.netos.network.IValve;
import cj.netos.network.NetworkFrame;
import cj.studio.ecm.net.CircuitException;

public class CheckSecurityValve implements IValve {
    @Override
    public void flow(NetworkFrame frame, IPipeline pipeline) throws CircuitException {
        pipeline.nextError(frame, new CircuitException("500","未使用网络节点插件"), this);
    }

    @Override
    public void nextError(NetworkFrame frame, Throwable error, IPipeline pipeline) throws CircuitException {
        pipeline.nextError(frame, error, this);
    }

    public CheckSecurityValve(INetworkServiceProvider site) {

    }
}
