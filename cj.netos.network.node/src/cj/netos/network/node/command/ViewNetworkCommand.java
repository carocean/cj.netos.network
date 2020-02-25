package cj.netos.network.node.command;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.IPrincipal;
import cj.netos.network.NetworkFrame;
import cj.netos.network.node.ChannelWriter;
import cj.netos.network.node.INetwork;
import cj.netos.network.node.INetworkCommand;
import cj.netos.network.node.INetworkContainer;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.gson2.com.google.gson.Gson;
import cj.ultimate.util.StringUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ViewNetworkCommand implements INetworkCommand {
    private final INetworkContainer networkContainer;
    private final ChannelWriter channelWriter;

    @Override
    public void exec(NetworkFrame frame, IPrincipal principal, Channel channel) throws CircuitException {
        String name = frame.rootName();
        if (StringUtil.isEmpty(name)) {
            CircuitException e = new CircuitException("500", "请求地址错误，未能确定访问的网络");
            throw e;
        }
        String viewmember = frame.parameter("viewMember");
        INetwork nw = networkContainer.openNetwork(name);
        if (nw == null) {
            return;
        }
        ByteBuf bb = Unpooled.buffer();
        Map<String, Object> info = new HashMap<>();
        info.put("name", nw.getName());
        info.put("title", nw.getTitle());
        info.put("frontendCastmode", nw.getFrontendCastmode());
        info.put("backendCastmode", nw.getBackendCastmode());
        info.put("backendMemberCount", nw.backendMemberCount());
        info.put("frontendMemberCount", nw.frontendMemberCount());
        if ("frontend".equals(viewmember)) {
            String[] frontendMembers = nw.listFrontendMembers();
            info.put("frontendMembers", frontendMembers);
        }
        if ("backend".equals(viewmember)) {
            String[] frontendMembers = nw.listBackendMembers();
            info.put("backendMembers", frontendMembers);
        }

        bb.writeBytes(new Gson().toJson(info).getBytes());
        NetworkFrame back = new NetworkFrame("viewNetwork /system/notify/ network/1.0", bb);
        if (principal != null) {
            back.head("sender-person", principal.principal());
            back.head("sender-peer", principal.peer());
        }
        back.head("sender-network", name);
        channelWriter.write(channel, back);
    }

    @Override
    public String getInstruction() {
        return "viewNetwork";
    }

    public ViewNetworkCommand(INetworkServiceProvider site) {
        networkContainer = (INetworkContainer) site.getService("$.network.networkContainer");
        channelWriter = new ChannelWriter();
    }
}
