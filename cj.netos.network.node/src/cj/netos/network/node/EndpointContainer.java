package cj.netos.network.node;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.IPrincipal;
import cj.netos.network.node.eventloop.EventTask;
import cj.studio.ecm.net.CircuitException;
import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;

public class EndpointContainer implements IEndpointerContainer {
    Map<String, IEndpointer> endpointers;//key是person/device
    Map<String, String> index;//key是channelid;value是person/device
    IPump pump;
    public EndpointContainer(INetworkServiceProvider site) {
        endpointers = new HashMap<>();
        index = new HashMap<>();
        pump = (IPump) site.getService("$.network.pump");
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
        endpointer.close();
        index.remove(id);
        endpointers.remove(key);
    }

    @Override
    public void online(Channel channel, IPrincipal principal) {
        IEndpointer endpointer = new DefaultEndpointer(channel, principal);
        endpointers.put(principal.key(),endpointer);
        index.put(channel.id().asLongText(), principal.key());

        //告知有拉取任务
        EventTask task=new EventTask(Direction.downstream,principal.key(),null);
        pump.arriveDownstream(task);
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
