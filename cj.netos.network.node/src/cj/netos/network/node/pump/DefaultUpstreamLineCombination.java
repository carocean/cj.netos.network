package cj.netos.network.node.pump;

import cj.netos.network.BackendCastmode;
import cj.netos.network.NetworkFrame;
import cj.netos.network.node.*;
import cj.netos.network.node.eventloop.ILine;
import cj.netos.network.node.eventloop.ILineCombination;
import cj.netos.network.node.eventloop.IReceiver;
import cj.netos.network.node.eventloop.Task;
import cj.studio.ecm.net.CircuitException;

//功能：从endport的上行队列中拉取消息分发给其它endport
public class DefaultUpstreamLineCombination implements ILineCombination {
    private final INetworkContainer networkContainer;
    private final IEndportContainer endportContainer;

    public DefaultUpstreamLineCombination(INetworkContainer networkContainer, IEndportContainer endportContainer) {
        this.networkContainer = networkContainer;
        this.endportContainer = endportContainer;
    }

    @Override
    public void combine(ILine line) {
        line.accept(new CastReceiver(networkContainer,endportContainer));
    }


}
