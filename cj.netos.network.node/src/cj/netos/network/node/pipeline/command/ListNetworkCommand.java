package cj.netos.network.node.pipeline.command;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.IPrincipal;
import cj.netos.network.NetworkFrame;
import cj.netos.network.node.ChannelWriter;
import cj.netos.network.node.INetwork;
import cj.netos.network.node.INetworkCommand;
import cj.netos.network.node.INetworkContainer;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.gson2.com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListNetworkCommand implements INetworkCommand {

    private final INetworkContainer networkContainer;
    ChannelWriter channelWriter;

    public ListNetworkCommand(INetworkServiceProvider site) {
        channelWriter = new ChannelWriter();
        networkContainer = (INetworkContainer) site.getService("$.network.networkContainer");
    }

    @Override
    public void exec(NetworkFrame frame, IPrincipal principal, Channel channel) throws CircuitException {
        ByteBuf bb = Unpooled.buffer();
        Map<String, Object> info = new HashMap<>();
        info.put("isAutoCreate", networkContainer.isAutoCreate());
        info.put("eventNetwork", networkContainer.getEventNetwork());
        List<Map<String, Object>> list = new ArrayList<>();
        for (String key : networkContainer.enumNetwork()) {
            INetwork nw = networkContainer.openNetwork(key);
            if (nw == null) {
                continue;
            }
            Map<String, Object> obj = new HashMap<>();
            obj.put("name", nw.getName());
            obj.put("title", nw.getTitle());
            obj.put("frontendCastmode", nw.getFrontendCastmode());
            obj.put("backendCastmode", nw.getBackendCastmode());
            obj.put("frontendMemberCount", nw.frontendMemberCount());
            obj.put("backendMemberCount", nw.backendMemberCount());
            list.add(obj);
            nw.close();
        }
        info.put("networks", list);
        bb.writeBytes(new Gson().toJson(info).getBytes());
        NetworkFrame back = new NetworkFrame(String.format("listNetwork /system/notify/ network/1.0"), bb);
        if (principal != null) {
            back.head("sender-person", principal.principal());
            back.head("sender-peer", principal.peer());
        }
        channelWriter.write(channel, back);
    }

    @Override
    public String getInstruction() {
        return "listNetwork";
    }

}
