package cj.netos.network.node.eventloop;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.node.Direction;
import cj.studio.ecm.net.CircuitException;

public interface ILine {
    String key();

    Direction direction();

    ILine accept(IReceiver receiver);

    void input(EventTask task) throws CircuitException;

    void nextInput(EventTask task, IReceiver current) throws CircuitException;

    void error(EventTask task, Throwable e);

    INetworkServiceProvider site();
}
