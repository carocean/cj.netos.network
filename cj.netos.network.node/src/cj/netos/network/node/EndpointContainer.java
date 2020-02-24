package cj.netos.network.node;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.IPrincipal;
import cj.netos.network.NetworkFrame;
import cj.netos.network.Sender;
import cj.studio.ecm.net.CircuitException;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.apache.jdbm.DB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EndpointContainer implements IEndpointerContainer {
    Map<String, IEndpointer> endpointers;//key是person/device
    Map<String, String> index;//key是channelid;value是person/device
    INetworkContainer networkContainer;
    INetworkServiceProvider site;

    public EndpointContainer(INetworkServiceProvider site) {
        this.site = site;
        endpointers = new HashMap<>();
        index = new HashMap<>();
        networkContainer = (INetworkContainer) site.getService("$.network.networkContainer");
    }

    @Override
    public void onChannelInactive(Channel channel) throws CircuitException {
        String id = channel.id().asLongText();
        String key = index.get(id);
        if (key == null) {
            return;
        }
        IEndpointer endpointer = endpointers.get(key);
        if (endpointer == null) {
            return;
        }
        offline(endpointer);

        Map<String, IEndpointerSink> sinks = endpointer.getSinks();
        AttributeKey<IPrincipal> keyP = AttributeKey.valueOf("Principal-Key");
        Attribute<IPrincipal> attribute = channel.attr(keyP);
        if (attribute != null) {
            IPrincipal principal = attribute.get();
            for (IEndpointerSink sink : sinks.values()) {
                if (sink == null) {
                    continue;
                }
                networkContainer.leaveNetwork(principal, sink.getNetwork());
            }
        }
        endpointer.close();
        index.remove(id);
        endpointers.remove(key);

    }

    @Override
    public IEndpointer availableEndpoint() {
        if (endpointers.isEmpty()) {
            return null;
        }
        for (Map.Entry<String, IEndpointer> entry : endpointers.entrySet()) {
            return entry.getValue();
        }
        return null;
    }

    @Override
    public IEndpointer endpoint(String key) {
        return endpointers.get(key);
    }

    @Override
    public IEndpointer openEndpoint(IPrincipal principal, Channel channel) throws CircuitException {
        String key = String.format("%s/%s", principal.principal(), principal.peer());
        if (endpointers.containsKey(key)) {
            return endpointers.get(key);
        }

        return _createEndpoint(key, principal, channel);
    }


    private IEndpointer _createEndpoint(String key, IPrincipal principal, Channel channel) throws CircuitException {
        IEndpointer endpointer = new DefaultEndpointer();
        endpointer.open(principal, channel, site);
        endpointers.put(key, endpointer);
        index.put(channel.id().asLongText(), key);
        online(endpointer);
        return endpointer;
    }

    protected void offline(IEndpointer endpointer) throws CircuitException {
        IPrincipal principal = endpointer.getPrincipal();

        NetworkFrame back = new NetworkFrame("offline /systemm/notify/ network/1.0");
        if (principal != null) {
            back.head("sender-person", principal.principal());
            back.head("sender-peer", principal.peer());
        }
        back.head("status", "200");
        back.head("message", "OK");
        endpointer.getChannelWriter().write(endpointer.getChannel(), back);

        NetworkFrame frame = new NetworkFrame(String.format("offline /%s/notify/ network/1.0", this.networkContainer.getEventNetwork()));
        endpointer.upstream(principal, frame);
    }

    protected void online(IEndpointer endpointer) throws CircuitException {
        IPrincipal principal = endpointer.getPrincipal();

        NetworkFrame back = new NetworkFrame("online /system/notify/ network/1.0");
        if (principal != null) {
            back.head("sender-person", principal.principal());
            back.head("sender-peer", principal.peer());
        }
        back.head("status", "200");
        back.head("message", "OK");
        endpointer.getChannelWriter().write(endpointer.getChannel(), back);

        NetworkFrame frame = new NetworkFrame(String.format("online /%s/notify/ network/1.0", this.networkContainer.getEventNetwork()));
        endpointer.upstream(principal, frame);
    }

    @Override
    public void onJoinNetwork(IPrincipal principal, String network) {
        String key = String.format("%s/%s", principal.principal(), principal.peer());
        IEndpointer endpointer = this.endpointers.get(key);
        if (endpointer == null) {
            return;
        }
        endpointer.joinNetwork(network);
    }

    @Override
    public void onLeaveNetwork(IPrincipal principal, String network) {
        String key = String.format("%s/%s", principal.principal(), principal.peer());
        IEndpointer endpointer = this.endpointers.get(key);
        if (endpointer == null) {
            return;
        }
        endpointer.leaveNetwork(network);
    }


    @Override
    public ISinkPull createSinkPuller(String endpoint, String network) {
        DB db = (DB) site.getService("$.network.db");
        String dbname = String.format("endpoint.sink://%s/%s", endpoint, network);
        List<?> _storableList = db.getLinkedList(dbname);
        if (_storableList == null) {
            _storableList = db.createLinkedList(dbname);
        }
        return new _SinkPuller(_storableList, db);
    }

    class _SinkPuller implements ISinkPull {
        List<?> storableList;
        DB db;

        public _SinkPuller(List<?> storableList, DB db) {
            this.storableList = storableList;
            this.db = db;
        }

        @Override
        public NetworkFrame pullFirst() {
            if (storableList.isEmpty()) {
                return null;
            }
            byte[] bytes = null;
            try {
                bytes = (byte[]) storableList.get(0);
            } catch (IndexOutOfBoundsException e) {
                return null;
            }
            return new NetworkFrame(bytes);
        }

        @Override
        public void removeFirst() {
            if (storableList.isEmpty()) {
                return;
            }
            storableList.remove(0);
            db.commit();
        }
    }
}
