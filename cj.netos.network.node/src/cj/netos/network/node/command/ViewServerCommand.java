package cj.netos.network.node.command;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.IPrincipal;
import cj.netos.network.NetworkFrame;
import cj.netos.network.Sender;
import cj.netos.network.node.*;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.gson2.com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;

public class ViewServerCommand extends AbastractNetworkCommand implements INetworkCommand {
    private final IEndpointerContainer endpointerContainer;
    INetworkNodeConfig config;

    public ViewServerCommand(INetworkServiceProvider site) {
        config = (INetworkNodeConfig) site.getService("$.network.config");
        endpointerContainer = (IEndpointerContainer) site.getService("$.network.endpointerContainer");
    }

    @Override
    public void exec(NetworkFrame frame, IPrincipal principal, Channel channel) throws CircuitException {
        ServerInfo info = config.getServerInfo();
        ByteBuf bb = Unpooled.buffer();
        Map<String, Object> map = new HashMap<>();
        map.put("host", String.format("%s://%s:%s", info.getProtocol(), info.getHost(), info.getPort()));
        map.put("openports", info.getOpenports());
        bb.writeBytes(new Gson().toJson(map).getBytes());
        ByteBuf copy=bb.copy();
        NetworkFrame back = new NetworkFrame(String.format("viewServer /system/notify/ network/1.0"),bb);
        if (principal != null) {
            back.head("sender-person", principal.principal());
            back.head("sender-peer", principal.peer());
        }
        write(channel, back);

        NetworkFrame f = new NetworkFrame(String.format("viewServer /%s/notify/ network/1.0", config.getNetworkConfig().getEventNetwork()), copy);
        IEndpointer endpointer = endpointerContainer.endpoint(principal.key());
        endpointer.upstream(principal,f);
    }

    @Override
    public String getInstruction() {
        return "viewServer";
    }
}
