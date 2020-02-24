package cj.netos.network.node.command;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.IPrincipal;
import cj.netos.network.NetworkFrame;
import cj.netos.network.Sender;
import cj.netos.network.node.*;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.gson2.com.google.gson.Gson;
import cj.ultimate.util.StringUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListNetworkCommand extends AbastractNetworkCommand implements INetworkCommand {
    private final IEndpointerContainer endpointerContainer;
    INetworkContainer networkContainer;

    public ListNetworkCommand(INetworkServiceProvider site) {
        networkContainer = (INetworkContainer) site.getService("$.network.networkContainer");
        endpointerContainer = (IEndpointerContainer) site.getService("$.network.endpointerContainer");
    }

    @Override
    public void exec(NetworkFrame frame, IPrincipal principal, Channel channel) throws CircuitException {
        ByteBuf bb = Unpooled.buffer();
        Map<String, Object> info = new HashMap<>();
        info.put("isAutoCreate", networkContainer.isAutoCreate());
        info.put("eventNetwork", networkContainer.getEventNetwork());
        List<Map<String, Object>> list = new ArrayList<>();
        for (String key : networkContainer.enumNetwork()) {
            INetwork nw = networkContainer.getNetwork(key);
            if (nw == null) {
                continue;
            }
            Map<String, Object> obj = new HashMap<>();
            obj.put("name", nw.getName());
            obj.put("title", nw.getTitle());
            obj.put("frontendCastmode", nw.getFrontendCastmode());
            obj.put("backendCastmode", nw.getBackendCastmode());
            obj.put("frontendSinCount", nw.enumFrontendSinkKey().size());
            obj.put("backendSinkKey", nw.enumBackendSinkKey().size());
            list.add(obj);
        }
        info.put("networks", list);
        bb.writeBytes(new Gson().toJson(info).getBytes());
        ByteBuf copy=bb.copy();

        NetworkFrame back = new NetworkFrame(String.format("listNetwork /system/notify/ network/1.0"),bb);
        if (principal != null) {
            back.head("sender-person", principal.principal());
            back.head("sender-peer", principal.peer());
        }
        write(channel, back);

        NetworkFrame f = new NetworkFrame(String.format("listNetwork /%s/notify/ network/1.0", networkContainer.getEventNetwork()), copy);
        IEndpointer endpointer = endpointerContainer.endpoint(principal.key());
        endpointer.upstream(principal, f);
    }

    @Override
    public String getInstruction() {
        return "listNetwork";
    }
}
