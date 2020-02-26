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

public class AuthNotifyNetworkCommand implements INetworkCommand {
    private final IEndportContainer endportContainer;
    private IEndpointerContainer endpointerContainer;
    ChannelWriter channelWriter;
    @Override
    public void exec(NetworkFrame frame, IPrincipal principal, Channel channel) throws CircuitException {
        if (endpointerContainer.hasEndpointer(principal.key())) {
            return;
        }
        _doResponse(principal,channel);
        endportContainer.openport(principal);
        endpointerContainer.online(channel, principal);

    }

    private void _doResponse(IPrincipal principal, Channel channel) {
        ByteBuf bb = Unpooled.buffer();
        Map<String, Object> map = new HashMap<>();
        bb.writeBytes(new Gson().toJson(map).getBytes());
        NetworkFrame back = new NetworkFrame(String.format("auth / network/1.0"), bb);
        if (principal != null) {
            back.head("sender-person", principal.principal());
            back.head("sender-peer", principal.peer());
        }
        channelWriter.write(channel, back);
    }

    @Override
    public String getInstruction() {
        return "auth";
    }

    public AuthNotifyNetworkCommand(INetworkServiceProvider site) {
        endpointerContainer = (IEndpointerContainer) site.getService("$.network.endpointerContainer");
        endportContainer = (IEndportContainer) site.getService("$.network.endportContainer");
        channelWriter = new ChannelWriter();
        NetworkNodeConfig config = (NetworkNodeConfig) site.getService("$.network.config");
    }
}
