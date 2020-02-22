package cj.netos.network.node.eventloop;

import cj.studio.ecm.net.CircuitException;

public interface IReceiver {
    void receive(Task task) throws CircuitException;

    void error(Task task, Throwable e);

}
