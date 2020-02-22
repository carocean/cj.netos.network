package cj.netos.network.node.eventloop;

import cj.netos.network.node.Direction;
import cj.studio.ecm.net.CircuitException;

public interface ILine {
    String key();

    Direction direction();

    void accept(IReceiver receiver);

    void input(Task task) throws CircuitException;

    void error(Task task, Throwable e);

}
