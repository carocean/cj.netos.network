package cj.netos.network.node.command;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.IPrincipal;
import cj.netos.network.NetworkFrame;
import cj.netos.network.node.*;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.gson2.com.google.gson.Gson;
import cj.ultimate.util.StringUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;

public class ViewNetworkCommand extends AbastractNetworkCommand implements INetworkCommand {
    private final IEndpointerContainer endpointerContainer;
    INetworkContainer networkContainer;

    public ViewNetworkCommand(INetworkServiceProvider site) {
        networkContainer = (INetworkContainer) site.getService("$.network.networkContainer");
        endpointerContainer = (IEndpointerContainer) site.getService("$.network.endpointerContainer");
    }

    @Override
    public void exec(NetworkFrame frame, IPrincipal principal, Channel channel) throws CircuitException {
        String name = frame.rootName();
        if (StringUtil.isEmpty(name)) {
            CircuitException e = new CircuitException("500", "请求地址错误，未能确定访问的网络");
            throw e;
        }
        INetwork nw = networkContainer.getNetwork(name);
        if (nw == null) {
            return;
        }
        ByteBuf bb = Unpooled.buffer();
        Map<String, Object> info = new HashMap<>();
        info.put("name", nw.getName());
        info.put("title", nw.getTitle());
        info.put("frontendCastmode", nw.getFrontendCastmode());
        info.put("backendCastmode", nw.getBackendCastmode());
        info.put("backendSinks", nw.enumBackendSinkKey());
        info.put("frontendSinks", nw.enumFrontendSinkKey());
        bb.writeBytes(new Gson().toJson(info).getBytes());
        ByteBuf copy=bb.copy();
        NetworkFrame back = new NetworkFrame("viewNetwork /system/notify/ network/1.0",bb);
        if (principal != null) {
            back.head("sender-person", principal.principal());
            back.head("sender-peer", principal.peer());
        }
        back.head("sender-network", name);
        write(channel, back);

        NetworkFrame f = new NetworkFrame(String.format("viewNetwork /%s/notify network/1.0", networkContainer.getEventNetwork()), copy);
        IEndpointer endpointer = endpointerContainer.endpoint(principal.key());
        endpointer.upstream(principal, f);

    }

    @Override
    public String getInstruction() {
        return "viewNetwork";
    }

}
