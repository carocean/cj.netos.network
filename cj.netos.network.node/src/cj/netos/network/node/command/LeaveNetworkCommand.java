package cj.netos.network.node.command;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.IPrincipal;
import cj.netos.network.NetworkFrame;
import cj.netos.network.node.*;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.gson2.com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;

public class LeaveNetworkCommand implements INetworkCommand {

    private final INetworkContainer networkContainer;
    private final IEndpointerContainer endpointerContainer;
    private final IEndportContainer endportContainer;
    private final ChannelWriter channelWriter;

    public LeaveNetworkCommand(INetworkServiceProvider site) {
        networkContainer = (INetworkContainer) site.getService("$.network.networkContainer");
        endpointerContainer = (IEndpointerContainer) site.getService("$.network.endpointerContainer");
        endportContainer = (IEndportContainer) site.getService("$.network.endportContainer");
        channelWriter = new ChannelWriter();
    }

    @Override
    public void exec(NetworkFrame frame, IPrincipal principal, Channel channel) throws CircuitException {
        String networkName = frame.rootName();
        networkContainer.leaveNetwork(networkName, principal);
        IEndpointer endpointer = endpointerContainer.endpointer(principal.key());
        endpointer.removeListenNetwork(networkName);
        IEndport endport = endportContainer.openport(principal.key());
        endport.getInfo().removeNetworkListenmode(networkName);

        NetworkFrame back = new NetworkFrame(String.format("leaveNetwork /%s network/1.0",networkName));
        if (principal != null) {
            back.head("sender-person", principal.principal());
            back.head("sender-peer", principal.peer());
        }
        channelWriter.write(channel, back);
    }

    @Override
    public String getInstruction() {
        return "leaveNetwork";
    }

}
