package cj.netos.network.node;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.IPrincipal;
import cj.netos.network.NetworkFrame;
import cj.studio.ecm.CJSystem;
import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//终结点，它拥有一个接收队列
public class DefaultEndpointer extends ChannelWriter implements IEndpointer {
    INetworkServiceProvider site;
    IPrincipal principal;
    Channel channel;
    Map<String, IEndpointerSink> sinks;
    String eventNetwork;

    @Override
    public void close() {
        for (IEndpointerSink sink : sinks.values()) {
            sink.close();
        }
        sinks.clear();
        site = null;
    }
    @Override
    public  ChannelWriter getChannelWriter(){
        return  this;
    }
    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    public String key() {
        return String.format("%s/%s", principal.principal(), principal.peer());
    }

    @Override
    public IPrincipal getPrincipal() {
        return principal;
    }

    @Override
    public Map<String, IEndpointerSink> getSinks() {
        return sinks;
    }

    @Override
    public void open(IPrincipal principal, Channel channel, INetworkServiceProvider site) {
        this.site = site;
        this.principal = principal;
        this.channel = channel;
        this.sinks = new ConcurrentHashMap<>();
        INetworkNodeConfig config = (INetworkNodeConfig) site.getService("$.network.config");
        this.eventNetwork = config.getNetworkConfig().getEventNetwork();
    }

    @Override
    public void joinNetwork(String network) {
        if (sinks.containsKey(network)) {
            return;
        }
        IEndpointerSink sink = new DefaultEndpointerSink();
        sink.open(key(), network, site);
        sinks.put(network, sink);
    }

    @Override
    public void leaveNetwork(String network) {
        if (sinks.containsKey(network)) {
            IEndpointerSink sink = sinks.get(network);
            sink.close();
            sinks.remove(network);
        }
    }

    private boolean isSendToEventNetwork(NetworkFrame frame) {
        return "NETWORK/1.0".equalsIgnoreCase(frame.protocol()) && frame.rootName().equals(eventNetwork);
    }

    @Override
    public void upstream(IPrincipal principal, NetworkFrame frame) {
        if (principal != null) {
            frame.head("sender-person", principal.principal());
            frame.head("sender-peer", principal.peer());
        }
        if (!frame.containsHead("status")) {
            frame.head("status", "200");
        }
        if (!frame.containsHead("message")) {
            frame.head("message", "OK");
        }
        String network = frame.rootName();
        if (!this.sinks.containsKey(network)) {
            if (!isSendToEventNetwork(frame)) {
                CJSystem.logging().warn(getClass(), String.format("禁止向非侦听的网络%s发送，侦被丢弃", network));
                return;
            }
            IEndpointerSink sink = new DefaultEndpointerSink();
            sink.open(key(), network, site);
            sinks.put(network, sink);
        }
        IEndpointerSink sink = sinks.get(network);
        sink.write(frame);
    }

    @Override
    public synchronized boolean downstream(NetworkFrame frame, String fromNetwork) {
        write(channel, frame);
        return true;
    }

}
