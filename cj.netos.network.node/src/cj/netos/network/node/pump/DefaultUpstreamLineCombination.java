package cj.netos.network.node.pump;

import cj.netos.network.node.*;
import cj.netos.network.node.eventloop.ILine;
import cj.netos.network.node.eventloop.ILineCombination;
import cj.netos.network.node.eventloop.ITaskQueue;

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
        ITaskQueue queue = (ITaskQueue) line.site().getService("$.pump.downstream.queue");
        line.accept(new CastFrameToEndport(networkContainer, endportContainer))
                .accept(new PutDownstreamEventTask(queue));
    }


}
