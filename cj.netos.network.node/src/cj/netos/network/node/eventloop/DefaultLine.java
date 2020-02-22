package cj.netos.network.node.eventloop;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.node.Direction;
import cj.studio.ecm.net.CircuitException;

public class DefaultLine implements ILine {
    INetworkServiceProvider site;
    IReceiver receiver;
    Direction direction;
    String key;

    public DefaultLine(INetworkServiceProvider site, String key, Direction direction) {
        this.site = site;
        this.key = key;
        this.direction = direction;
    }
    @Override
    public void accept(IReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void input(Task task) throws CircuitException {
        if (receiver == null) {
            return;
        }
        receiver.receive(task);
    }

    @Override
    public void error(Task task, Throwable e) {
        if (receiver == null) {
            return;
        }
        receiver.error(task, e);
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public Direction direction() {
        return direction;
    }
}
