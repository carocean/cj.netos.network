package cj.netos.network.node.pipeline.command;

import cj.netos.network.*;
import cj.netos.network.node.ChannelWriter;
import cj.netos.network.node.INetworkCommand;
import cj.netos.network.node.INetworkContainer;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.util.StringUtil;
import io.netty.channel.Channel;

public class CreateNetworkCommand implements INetworkCommand {
    ChannelWriter channelWriter;
    private final INetworkContainer networkContainer;

    public CreateNetworkCommand(INetworkServiceProvider site) {
        networkContainer = (INetworkContainer) site.getService("$.network.networkContainer");
        channelWriter = new ChannelWriter();
    }

    @Override
    public void exec(NetworkFrame frame, IPrincipal principal, Channel channel) throws CircuitException {
        String name = frame.rootName();
        if (StringUtil.isEmpty(name)) {
            throw new CircuitException("500", "请求地址错误，未能确定访问的网络");
        }
        String title = frame.parameter("title");
        if (StringUtil.isEmpty(title)) {
            throw new CircuitException("500", "缺少参数:title");
        }
        String str_frontendCastmode = frame.parameter("frontendCastmode");
        FrontendCastmode frontendCastmode = null;
        if (StringUtil.isEmpty(str_frontendCastmode)) {
            frontendCastmode = FrontendCastmode.selectcast;
        } else {
            frontendCastmode = FrontendCastmode.valueOf(str_frontendCastmode);
        }

        String str_backendCastmode = frame.parameter("backendCastmode");
        BackendCastmode backendCastmode = null;
        if (StringUtil.isEmpty(str_backendCastmode)) {
            backendCastmode = BackendCastmode.unicast;
        } else {
            backendCastmode = BackendCastmode.valueOf(str_backendCastmode);
        }
        networkContainer.createNetwork(principal, name, title, frontendCastmode, backendCastmode);

        NetworkFrame back = new NetworkFrame("createNetwork /system/notify/ network/1.0");
        if (principal != null) {
            back.head("sender-person", principal.principal());
            back.head("sender-peer", principal.peer());
        }
        back.head("sender-network", name);
        channelWriter.write(channel, back);
    }

    @Override
    public String getInstruction() {
        return "createNetwork";
    }

}
