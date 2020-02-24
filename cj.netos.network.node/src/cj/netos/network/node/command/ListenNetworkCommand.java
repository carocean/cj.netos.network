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

import java.util.HashMap;
import java.util.Map;

public class ListenNetworkCommand extends AbastractNetworkCommand implements INetworkCommand {
    INetworkContainer networkContainer;
    IEndpointerContainer endpointerContainer;

    public ListenNetworkCommand(INetworkServiceProvider site) {
        networkContainer = (INetworkContainer) site.getService("$.network.networkContainer");
        endpointerContainer = (IEndpointerContainer) site.getService("$.network.endpointerContainer");
    }

    @Override
    public void exec(NetworkFrame frame, IPrincipal principal, Channel channel) throws CircuitException {
        //1.加入网络
        //2.通知节点容器更新终结点配置
        String name = frame.rootName();
        if (StringUtil.isEmpty(name)) {
            throw new CircuitException("500", "请求地址错误，未能确定访问的网络");
        }
        String mode = frame.head("transfer-mode");
        if (StringUtil.isEmpty(mode)) {
            mode = "push";
        }
        String _isjoinToFrontend = frame.parameter("isJoinToFrontend");
        if (StringUtil.isEmpty(_isjoinToFrontend)) {
            _isjoinToFrontend = "true";
        }
        boolean joinToFrontend = Boolean.valueOf(_isjoinToFrontend);
        networkContainer.joinNetwork(principal, name, joinToFrontend);
        endpointerContainer.onJoinNetwork(principal, name);

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

        NetworkFrame back = new NetworkFrame("joinNetwork /system/notify/ network/1.0",bb);
        if (principal != null) {
            back.head("sender-person", principal.principal());
            back.head("sender-peer", principal.peer());
        }
        back.head("sender-network", name);
        write(channel, back);

        NetworkFrame f = new NetworkFrame(String.format("joinNetwork /%s/notify/ network/1.0", networkContainer.getEventNetwork()), copy);
        IEndpointer endpointer = endpointerContainer.endpoint(principal.key());
        endpointer.upstream(principal, f);
    }

    @Override
    public String getInstruction() {
        return "listenNetwork";
    }

}
