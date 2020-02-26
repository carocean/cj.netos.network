package cj.netos.network.node.command;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.IPrincipal;
import cj.netos.network.ListenMode;
import cj.netos.network.NetworkFrame;
import cj.netos.network.node.*;
import cj.netos.network.node.eventloop.EventTask;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.gson2.com.google.gson.Gson;
import cj.ultimate.util.StringUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;

public class ListenNetworkCommand implements INetworkCommand {
    private final IPump pump;
    private final ChannelWriter channelWriter;
    IEndportContainer endportContainer;
    INetworkContainer networkContainer;
    IEndpointerContainer endpointerContainer;

    @Override
    public void exec(NetworkFrame frame, IPrincipal principal, Channel channel) throws CircuitException {
        String networkName = frame.rootName();
        if (!networkContainer.containsNetwork(networkName)) {
            throw new CircuitException("404", "网络不存在:" + networkName);
        }
        IEndpointer endpointer = endpointerContainer.endpointer(principal.key());
        endpointer.addListenNetwork(networkName);

        String _listenmode = frame.parameter("listenMode");
        if (StringUtil.isEmpty(_listenmode)) {
            _listenmode = "both";
        }
        ListenMode mode = ListenMode.valueOf(_listenmode);
        endportContainer.openport(principal).getInfo().addListenNetwork(networkName, mode);

        String _isjoinToFrontend = frame.parameter("isJoinToFrontend");
        if (StringUtil.isEmpty(_isjoinToFrontend)) {
            _isjoinToFrontend = "true";
        }
        boolean joinToFrontend = Boolean.valueOf(_isjoinToFrontend);
        networkContainer.joinNetwork(networkName, principal, joinToFrontend);

        //侦听后来一次拉取任务，因为可能有消息
        EventTask task = new EventTask(Direction.downstream, principal.key(), networkName);
        pump.arriveDownstream(task);


        NetworkFrame back = new NetworkFrame(String.format("joinNetwork /%s network/1.0",networkName));
        if (principal != null) {
            back.head("sender-person", principal.principal());
            back.head("sender-peer", principal.peer());
        }
        channelWriter.write(channel, back);
    }

    @Override
    public String getInstruction() {
        return "listenNetwork";
    }

    public ListenNetworkCommand(INetworkServiceProvider site) {
        networkContainer = (INetworkContainer) site.getService("$.network.networkContainer");
        endportContainer = (IEndportContainer) site.getService("$.network.endportContainer");
        pump = (IPump) site.getService("$.network.pump");
        endpointerContainer = (IEndpointerContainer) site.getService("$.network.endpointerContainer");
        channelWriter = new ChannelWriter();
    }
}
