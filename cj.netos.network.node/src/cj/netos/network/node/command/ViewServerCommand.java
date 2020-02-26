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

public class ViewServerCommand implements INetworkCommand {
    private final INetworkNodeConfig config;
    ChannelWriter channelWriter;

    @Override
    public void exec(NetworkFrame frame, IPrincipal principal, Channel channel) throws CircuitException {
        ServerInfo info = config.getServerInfo();
        ByteBuf bb = Unpooled.buffer();
        Map<String, Object> map = new HashMap<>();
        map.put("host", String.format("%s://%s:%s", info.getProtocol(), info.getHost(), info.getPort()));
        map.put("openports", info.getOpenports());
        bb.writeBytes(new Gson().toJson(map).getBytes());
        NetworkFrame back = new NetworkFrame(String.format("viewServer / network/1.0"), bb);
        if (principal != null) {
            back.head("sender-person", principal.principal());
            back.head("sender-peer", principal.peer());
        }
        channelWriter.write(channel, back);
    }

    @Override
    public String getInstruction() {
        return "viewServer";
    }

    public ViewServerCommand(INetworkServiceProvider site) {
        config = (INetworkNodeConfig) site.getService("$.network.config");
        channelWriter = new ChannelWriter();
        NetworkNodeConfig config = (NetworkNodeConfig) site.getService("$.network.config");
    }
}
