package cj.netos.network.node;

import cj.netos.network.Castmode;
import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.IPrincipal;
import cj.netos.network.NetworkFrame;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.net.CircuitException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

//逻辑网络
public class DefaultNetwork implements INetwork {

    private final INetworkServiceProvider site;
    private final String name;
    private final String title;
    private final Castmode frontendCastmode;
    private final Castmode backendCastmode;
    private Map<String, INetworkSink> frontendSinks;
    private List<String> index_frontendSinks;
    private Map<String, INetworkSink> backendSinks;
    private List<String> index_backendSinks;

    public DefaultNetwork(INetworkServiceProvider site, NetworkInfo networkInfo) {
        this.site = site;
        frontendSinks = new ConcurrentHashMap<>();
        backendSinks = new ConcurrentHashMap<>();
        this.index_backendSinks = new CopyOnWriteArrayList<>();
        this.index_frontendSinks = new CopyOnWriteArrayList<>();
        this.name = networkInfo.getName();
        this.title = networkInfo.getTitle();
        this.frontendCastmode = networkInfo.getFrontendCastMode();
        this.backendCastmode = networkInfo.getBackendCastmode();

    }

    public DefaultNetwork(INetworkServiceProvider site, String name, String title, Castmode frontendCastmode, Castmode backendCastmode) {
        this.site = site;
        frontendSinks = new HashMap<>();
        backendSinks = new HashMap<>();
        this.name = name;
        this.title = title;
        this.frontendCastmode = frontendCastmode;
        this.backendCastmode = backendCastmode;
    }

    @Override
    public void cast(NetworkFrame frame) throws CircuitException {
        switch (backendCastmode) {
            case unicast:
                _unicast(frame, backendSinks, index_backendSinks);
                break;
            case multicast:
                _multicast(frame, backendSinks, index_backendSinks);
                break;
            case selectcast:
                _selectcast(frame, backendSinks, index_backendSinks);
                break;
        }
        switch (frontendCastmode) {
            case unicast:
                _unicast(frame, frontendSinks, index_frontendSinks);
                break;
            case multicast:
                _multicast(frame, frontendSinks, index_frontendSinks);
                break;
            case selectcast:
                _selectcast(frame, frontendSinks, index_frontendSinks);
                break;
        }
    }

    private void _selectcast(NetworkFrame frame, Map<String, INetworkSink> sinks, List<String> index) {
        String to_person = frame.head("to-person");
        String to_peer = frame.head("to-peer");
        String key = String.format("%s/%s", to_person, to_peer);
        INetworkSink sink = sinks.get(key);
        if (sink == null) {
            CJSystem.logging().warn(getClass(), "未找到分发槽，该侦丢弃");
            return;
        }
        sink.write(frame);
    }

    private void _multicast(NetworkFrame frame, Map<String, INetworkSink> sinks, List<String> index) {
        for (Map.Entry<String, INetworkSink> entry : sinks.entrySet()) {
            if (entry == null) {
                continue;
            }
            INetworkSink sink = entry.getValue();
            sink.write(frame);
        }
    }

    private void _unicast(NetworkFrame frame, Map<String, INetworkSink> sinks, List<String> index) {
        if (index.isEmpty()) {
            return;
        }
        int pos = Math.abs(String.format("%s%s", System.currentTimeMillis(), frame.toString()).hashCode()) % index.size();
        String key = index.get(pos);
        INetworkSink sink = sinks.get(key);
        if (sink == null) {
            CJSystem.logging().warn(getClass(), "未找到分发目标，该侦丢弃");
            return;
        }
        sink.write(frame);
    }

    @Override
    public void join(IPrincipal principal, boolean joinToFrontend) {
        INetworkSink sink = new DefaultNetworkSink();
        sink.open(site, this.name, principal.principal(), principal.peer());
        if (joinToFrontend) {
            addFrontendSink(sink);
        } else {
            addBackendSink(sink);
        }
    }

    protected void addFrontendSink(INetworkSink sink) {
        if (frontendSinks.containsKey(sink.getKey())) {
            return;
        }
        frontendSinks.put(sink.getKey(), sink);
        index_frontendSinks.add(sink.getKey());
    }

    protected void addBackendSink(INetworkSink sink) {
        if (backendSinks.containsKey(sink.getKey())) {
            return;
        }
        backendSinks.put(sink.getKey(), sink);
        index_backendSinks.add(sink.getKey());
    }

    @Override
    public void leave(IPrincipal principal, boolean isLeaveFrontend) {
        if (isLeaveFrontend) {
            INetworkSink sink = getFrontendSink(principal);
            if (sink == null) {
                return;
            }
            sink.close();
            removeFrontendSink(sink);
        } else {
            INetworkSink sink = getBackendSink(principal);
            if (sink == null) {
                return;
            }
            sink.close();
            removeBackendSink(sink);
        }
    }

    protected void removeFrontendSink(INetworkSink sink) {
        frontendSinks.remove(sink.getKey());
    }

    protected void removeBackendSink(INetworkSink sink) {
        backendSinks.remove(sink.getKey());
    }

    @Override
    public INetworkSink getFrontendSink(String endpointKey) {
        return frontendSinks.get(endpointKey);
    }

    @Override
    public Set<String> enumFrontendSinkKey() {
        return frontendSinks.keySet();
    }

    @Override
    public INetworkSink getBackendSink(String endpointKey) {
        return backendSinks.get(endpointKey);
    }

    @Override
    public Set<String> enumBackendSinkKey() {
        return backendSinks.keySet();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public Castmode getFrontendCastmode() {
        return frontendCastmode;
    }

    @Override
    public Castmode getBackendCastmode() {
        return backendCastmode;
    }

    @Override
    public INetworkSink getFrontendSink(IPrincipal principal) {
        String key = String.format("%s/%s", principal.principal(), principal.peer());
        return frontendSinks.get(key);
    }

    @Override
    public INetworkSink getBackendSink(IPrincipal principal) {
        String key = String.format("%s/%s", principal.principal(), principal.peer());
        return backendSinks.get(key);
    }

}
