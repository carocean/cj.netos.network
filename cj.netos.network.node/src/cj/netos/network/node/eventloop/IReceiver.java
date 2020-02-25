package cj.netos.network.node.eventloop;

import cj.studio.ecm.net.CircuitException;

public interface IReceiver {
    void receive(EventTask task, ILine line) throws CircuitException;

    void error(EventTask task, Throwable e, ILine line);

}
