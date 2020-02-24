package cj.netos.network.node;

import cj.netos.network.*;
import cj.netos.network.node.eventloop.Task;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.net.CircuitException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

//逻辑网络
public class DefaultNetwork implements INetwork {

    private INetworkServiceProvider site;
    private final String name;
    private final String title;
    private final FrontendCastmode frontendCastmode;
    private final BackendCastmode backendCastmode;
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

    public DefaultNetwork(INetworkServiceProvider site, String name, String title, FrontendCastmode frontendCastmode, BackendCastmode backendCastmode) {
        this.site = site;
        frontendSinks = new HashMap<>();
        backendSinks = new HashMap<>();
        this.name = name;
        this.title = title;
        this.frontendCastmode = frontendCastmode;
        this.backendCastmode = backendCastmode;
    }

    @Override
    public void cast(Sender sender, NetworkFrame frame) throws CircuitException {
        _checkframeUrl(frame);

        switch (backendCastmode) {
            case unicast:
                _unicast(sender, frame, backendSinks, index_backendSinks);
                break;
            case multicast:
                _multicast(sender, frame, backendSinks, index_backendSinks);
                break;
            case selectcast:
                _selectcast(frame, backendSinks, index_backendSinks);
                break;
            case forbiddenBackendCastButAllowFrontendUnicast:
                //前置过来的可向后置分发，而后置过来的被禁止在后置传播。但后置发来的消息可以在下面switch分发给前置
                if (!isBackendSender(sender)) {
                    _unicast(sender, frame, backendSinks, index_backendSinks);
                }
                break;
            case forbiddenBackendCastButAllowFrontendMulticast:
                if (!isBackendSender(sender)) {
                    _multicast(sender, frame, backendSinks, index_backendSinks);
                }
                break;
            case forbiddenBackendCastButAllowFrontendSelectcast:
                if (!isBackendSender(sender)) {
                    _selectcast(frame, backendSinks, index_backendSinks);
                }
                break;
        }

        switch (frontendCastmode) {
            case unicast:
                _unicast(sender, frame, frontendSinks, index_frontendSinks);
                break;
            case multicast:
                _multicast(sender, frame, frontendSinks, index_frontendSinks);
                break;
            case selectcast:
                _selectcast(frame, frontendSinks, index_frontendSinks);
                break;
        }
    }

    private boolean isBackendSender(Sender sender) {
        return backendSinks.containsKey(String.format("%s/%s", sender.getPerson(), sender.getPeer()));
    }

    private void _checkframeUrl(NetworkFrame frame) {
        String name = frame.rootName();
        if (!this.name.equals(name)) {
            String old = frame.url();
            String url = String.format("/%s%s", this.name, old);
            frame.url(url);
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

    private boolean _isSelf(String protocol, String sinkKey, String sendKey) {
        return !"network/1.0".equalsIgnoreCase(protocol) && sinkKey.equals(sendKey);
    }

    private void _multicast(Sender sender, NetworkFrame frame, Map<String, INetworkSink> sinks, List<String> index) {
        for (Map.Entry<String, INetworkSink> entry : sinks.entrySet()) {
            if (entry == null || /*不发自身但必须排除network/1.0协议*/_isSelf(frame.protocol(), entry.getKey(), sender.getKey())) {
                continue;
            }
            INetworkSink sink = entry.getValue();
            sink.write(frame);
        }
    }

    private void _unicast(Sender sender, NetworkFrame frame, Map<String, INetworkSink> sinks, List<String> index) {
        if (index.isEmpty()) {
            return;
        }
        int pos = Math.abs(String.format("%s%s", System.currentTimeMillis(), frame.toString()).hashCode()) % index.size();
        String key = "";
        if (!"network/1.0".equalsIgnoreCase(frame.protocol())) {
            List<String> copy = new ArrayList<>();
            copy.addAll(index);
            //不发自身
            copy.remove(sender.getKey());
            key = copy.get(pos);
        } else {
            key = index.get(pos);
        }
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
    public void leave(IPrincipal principal) {
        INetworkSink sink = getFrontendSink(principal);
        if (sink != null) {
            sink.close();
            removeFrontendSink(sink);
        }
        sink = getBackendSink(principal);
        if (sink != null) {
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
    public FrontendCastmode getFrontendCastmode() {
        return frontendCastmode;
    }

    @Override
    public BackendCastmode getBackendCastmode() {
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

    @Override
    public void close() {
        if (this.backendSinks != null)
            this.backendSinks.clear();
        if (this.frontendSinks != null)
            this.frontendSinks.clear();
        if (this.index_frontendSinks != null)
            this.index_frontendSinks.clear();
        if (this.index_backendSinks != null)
            this.index_backendSinks.clear();
        this.site = null;
    }
}
