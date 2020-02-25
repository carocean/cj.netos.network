package cj.netos.network.node.eventloop;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.node.Direction;
import cj.studio.ecm.net.CircuitException;

public class DefaultLine implements ILine {
    INetworkServiceProvider site;
    Direction direction;
    String key;
    _LinkEntry head;
    _LinkEntry tail;

    public DefaultLine(INetworkServiceProvider site, String key, Direction direction) {
        this.site = site;
        this.key = key;
        this.direction = direction;
    }

    @Override
    public INetworkServiceProvider site() {
        return site;
    }

    @Override
    public ILine accept(IReceiver receiver) {
        if (tail != null) {
            tail.next = new _LinkEntry(null, receiver);
            return this;
        }
        if (head == null) {
            head = new _LinkEntry(null, receiver);
        }
        if (tail == null) {
            tail = head;
        }
        return this;
    }

    @Override
    public void input(EventTask task) throws CircuitException {
        if (head == null) {
            return;
        }
        head.entry.receive(task, this);
    }

    @Override
    public void nextInput(EventTask task, IReceiver current) throws CircuitException {
        if (head == null)
            return;
        if (current == null) {
            head.entry.receive(task, this);
            return;
        }
        _LinkEntry linkEntry = lookforHead(current);
        if (linkEntry == null || linkEntry.next == null)
            return;
        linkEntry.next.entry.receive(task, this);
    }

    @Override
    public void error(EventTask task, Throwable e) {
        if (head == null) {
            return;
        }
        head.entry.error(task, e, this);
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public Direction direction() {
        return direction;
    }

    private _LinkEntry lookforHead(IReceiver formthis) {
        if (head == null)
            return null;
        _LinkEntry tmp = head;
        do {
            if (formthis.equals(tmp.entry)) {
                break;
            }
            tmp = tmp.next;
        } while (tmp.next != null);
        return tmp;
    }

    class _LinkEntry {
        _LinkEntry next;
        IReceiver entry;

        public _LinkEntry(_LinkEntry next, IReceiver entry) {
            this.next = next;
            this.entry = entry;
        }
    }
}
