package cj.netos.network;

import cj.netos.network.*;
import cj.studio.ecm.net.CircuitException;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;


public class DefaultPipeline implements IPipeline {
    LinkEntry head;
    private INetworkServiceProvider site;
    Object attachment;
    private IPrincipal principal;

    public DefaultPipeline(INetworkServiceProvider site) {
        this.site = site;
    }

    @Override
    public boolean isEmpty() {
        return head == null;
    }

    @Override
    public INetworkServiceProvider site() {
        return site;
    }

    @Override
    public void dispose() {
        LinkEntry next = head;
        LinkEntry prev = null;
        while (next != null) {

            prev = next;
            next = next.next;
            prev.next = null;
            prev.entry = null;
        }
        this.head = null;
    }

    @Override
    public void append(IValve valve) {
        if (head == null) {
            head = new LinkEntry(valve);
            return;
        }
        LinkEntry entry = getEndConstomerEntry();
        if (entry == null) {
            return;
        }
        LinkEntry lastEntry = entry.next;
        entry.next = new LinkEntry(valve);
        entry.next.next = lastEntry;
    }

    private LinkEntry getEndConstomerEntry() {
        if (head == null)
            return null;
        LinkEntry tmp = head;
        do {
            if (tmp.next == null)
                return tmp;
            tmp = tmp.next;
        } while (tmp != null);
        return null;
    }

    @Override
    public void remove(IValve valve) {
        if (head == null)
            return;
        LinkEntry tmp = head;
        do {
            if (valve.equals(tmp.next.entry)) {
                break;
            }
            tmp = tmp.next;
        } while (tmp.next != null);
        tmp.next = tmp.next.next;
    }

    @Override
    public void nextError(NetworkFrame e, Throwable error, IValve formthis) throws CircuitException {
        if (head == null) {
            return;
        }
        if (formthis == null) {
            head.entry.nextError(e, error, this);
            return;
        }
        LinkEntry linkEntry = lookforHead(formthis);
        if (linkEntry == null || linkEntry.next == null)
            return;
        linkEntry.next.entry.nextError(e, error, this);
    }

    @Override
    public void error(NetworkFrame event, Throwable e) throws CircuitException {
        if (head == null)
            return;
        nextError(event, e, null);
    }

    @Override
    public void input(NetworkFrame e) throws CircuitException {
        if (head == null)
            return;
        nextFlow(e, null);
    }

    @Override
    public void nextFlow(NetworkFrame e, IValve formthis) throws CircuitException {
        if (head == null)
            return;
        if (formthis == null) {
            head.entry.flow(e, this);
            return;
        }
        LinkEntry linkEntry = lookforHead(formthis);
        if (linkEntry == null || linkEntry.next == null)
            return;
        linkEntry.next.entry.flow(e, this);
    }

    @Override
    public IPrincipal principal() {
        if (principal == null) {
            Channel channel = (Channel) attachment;
            AttributeKey<IPrincipal> key = AttributeKey.valueOf("Principal-Key");
            Attribute<IPrincipal> attribute = channel.attr(key);
            if (attribute == null) {
                return null;
            }
            principal = attribute.get();
        }
        return principal;
    }

    @Override
    public void principal(IPrincipal principal) {
        this.principal = principal;
        AttributeKey<IPrincipal> key = AttributeKey.valueOf("Principal-Key");
        Channel channel = (Channel) attachment;
        channel.attr(key).set(principal);
    }

    @Override
    public Object attachment() {
        return attachment;
    }

    @Override
    public void attachment(Object attachment) {
        this.attachment = attachment;
    }

    private LinkEntry lookforHead(IValve formthis) {
        if (head == null)
            return null;
        LinkEntry tmp = head;
        do {
            if (formthis.equals(tmp.entry)) {
                break;
            }
            tmp = tmp.next;
        } while (tmp.next != null);
        return tmp;
    }

    class LinkEntry {
        LinkEntry next;
        IValve entry;

        public LinkEntry(IValve entry) {
            this.entry = entry;
        }

    }
}
