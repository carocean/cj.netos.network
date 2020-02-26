package cj.netos.network.node.command;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.IPrincipal;
import cj.netos.network.ListenMode;
import cj.netos.network.NetworkFrame;
import cj.netos.network.node.*;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.gson2.com.google.gson.Gson;
import cj.ultimate.util.StringUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

import java.util.*;

public class ViewNetworkCommand implements INetworkCommand {
    private final INetworkContainer networkContainer;
    private final ChannelWriter channelWriter;
    private final IEndportContainer endportContainer;
    private final IEndpointerContainer endpointerContainer;

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
        Map<String, Object> networkInfo = new HashMap<>();
        networkInfo.put("name", nw.getName());
        networkInfo.put("title", nw.getTitle());
        networkInfo.put("frontendCastmode", nw.getFrontendCastmode());
        networkInfo.put("backendCastmode", nw.getBackendCastmode());
        networkInfo.put("backendMemberCount", nw.backendMemberCount());
        networkInfo.put("frontendMemberCount", nw.frontendMemberCount());
        if ("frontend".equals(viewmember)) {
            String[] frontendMembers = nw.listFrontendMembers();
            networkInfo.put("frontendMembers", frontendMembers);
        }
        if ("backend".equals(viewmember)) {
            String[] frontendMembers = nw.listBackendMembers();
            networkInfo.put("backendMembers", frontendMembers);
        }
        Map<String, Object> endportMap = new HashMap<>();
        IEndport endport = endportContainer.openport(principal);
        if (endport != null) {
            endportMap.put("listener", endport.getInfo().getPrincipalKey());
            String[] arr = endport.getInfo().enumNetworkListenmode();
            List<Map<String, Object>> listenList = new ArrayList<>();
            for (String key : arr) {
                Map<String, Object> obj = new HashMap<>();
                ListenMode mode = endport.getInfo().getNetworkListenmode(key);
                obj.put("network", key);
                obj.put("listenMode", mode);
                listenList.add(obj);
            }
            endportMap.put("networks", listenList);
        }
        String[] endpointList = new String[0];
        IEndpointer endpointer = endpointerContainer.endpointer(principal.key());
        if (endpointer != null) {
            endpointList = endpointer.listenNetworks();
        }
        Map<String, Object> map = new HashMap<>();
        map.put("network", networkInfo);
        map.put("endport", endportMap);
        map.put("listening", endpointList);

        bb.writeBytes(new Gson().toJson(map).getBytes());

        NetworkFrame back = new NetworkFrame(String.format("viewNetwork /%s network/1.0", name), bb);
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
        endportContainer = (IEndportContainer) site.getService("$.network.endportContainer");
        networkContainer = (INetworkContainer) site.getService("$.network.networkContainer");
        endpointerContainer = (IEndpointerContainer) site.getService("$.network.endpointerContainer");
        channelWriter = new ChannelWriter();
        NetworkNodeConfig config = (NetworkNodeConfig) site.getService("$.network.config");
    }
}
