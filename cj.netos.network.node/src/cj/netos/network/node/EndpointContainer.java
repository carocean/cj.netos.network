package cj.netos.network.node;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.IPrincipal;
import cj.netos.network.NetworkFrame;
import cj.netos.network.node.eventloop.EventTask;
import cj.studio.ecm.net.CircuitException;
import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;

public class EndpointContainer implements IEndpointerContainer {
    private final INetworkNodeConfig config;
    Map<String, IEndpointer> endpointers;//key是person/device
    Map<String, String> index;//key是channelid;value是person/device
    IPump pump;

    public EndpointContainer(INetworkServiceProvider site) {
        endpointers = new HashMap<>();
        index = new HashMap<>();
        pump = (IPump) site.getService("$.network.pump");
        config = (INetworkNodeConfig) site.getService("$.network.config");
    }

    @Override
    public void offline(Channel channel) throws CircuitException {
        String id = channel.id().asLongText();
        String key = index.get(id);
        if (key == null) {
            return;
        }
        IEndpointer endpointer = endpointers.get(key);
        if (endpointer == null) {
            return;
        }
        NetworkFrame back = new NetworkFrame("offline / network/1.0");
        int pos = key.indexOf("/");
        String person = key.substring(0, pos);
        String peer = key.substring(pos + 1);
        back.head("sender-person", person);
        back.head("sender-peer", peer);
        back.head("status", "200");
        back.head("message", "OK");
        endpointer.write(back);

        endpointer.close();
        index.remove(id);
        endpointers.remove(key);
    }

    @Override
    public void online(Channel channel, IPrincipal principal) {
        IEndpointer endpointer = new DefaultEndpointer(channel, principal);
        endpointers.put(principal.key(), endpointer);
        index.put(channel.id().asLongText(), principal.key());

        NetworkFrame back = new NetworkFrame("online / network/1.0");
        if (principal != null) {
            back.head("sender-person", principal.principal());
            back.head("sender-peer", principal.peer());
        }
        back.head("status", "200");
        back.head("message", "OK");
        endpointer.write(back);
    }

    @Override
    public IEndpointer endpointer(String endpointerKey) {
        return endpointers.get(endpointerKey);
    }

    @Override
    public boolean hasEndpointer(String endpointerKey) {
        return endpointers.containsKey(endpointerKey);
    }

}
