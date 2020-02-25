package cj.netos.network.node.pump;

import cj.netos.network.node.Direction;
import cj.netos.network.node.eventloop.EventTask;
import cj.netos.network.node.eventloop.ILine;
import cj.netos.network.node.eventloop.IReceiver;
import cj.netos.network.node.eventloop.ITaskQueue;
import cj.studio.ecm.net.CircuitException;

public class PutDownstreamEventTask implements IReceiver {
    ITaskQueue queue;

    @Override
    public void receive(EventTask task, ILine line) throws CircuitException {
        queue.append(task);
    }

    @Override
    public void error(EventTask task, Throwable e, ILine line) {

    }

    public PutDownstreamEventTask(ITaskQueue queue) {
        this.queue = queue;
    }
}
