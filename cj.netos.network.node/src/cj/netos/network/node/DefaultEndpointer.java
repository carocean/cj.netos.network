package cj.netos.network.node;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.IPrincipal;
import cj.netos.network.NetworkFrame;
import cj.netos.network.TransferMode;
import cj.studio.ecm.CJSystem;
import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//终结点，它拥有一个接收队列
public class DefaultEndpointer implements IEndpointer {
    INetworkServiceProvider site;
    IPrincipal principal;
    Channel channel;
    Map<String, IEndpointerSink> sinks;

    @Override
    public void close() {
        for (IEndpointerSink sink : sinks.values()) {
            sink.close();
        }
        sinks.clear();
        site = null;
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
    }

    @Override
    public void joinNetwork(String network, TransferMode mode) {
        IEndpointerSink sink = new DefaultEndpointerSink();
        sink.open(key(), network, mode, site);
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


    @Override
    public void upstream(NetworkFrame frame) {
        String network = frame.rootName();
        if (!this.sinks.containsKey(network)) {
            CJSystem.logging().warn(getClass(), String.format("禁止向非侦听的网络%s发送，侦被丢弃", network));
            return;
        }
        IEndpointerSink sink = sinks.get(network);
        sink.write(frame);
    }

    @Override
    public synchronized boolean downstream(NetworkFrame frame, String fromNetwork) {
        IEndpointerSink sink = sinks.get(fromNetwork);
        TransferMode mode = TransferMode.push;
        if (sink != null) {
            mode = sink.getMode();
        }
        if (mode == TransferMode.push) {
            //非netty线程下不能回写
            ChannelWriter.write(channel, frame);
            return true;
        }
        //其下为拉而设置侦
//        if (_notEmpty.get() != null) {
//            return false;
//        }
//        _notEmpty.set(frame);
        return true;
    }

}
