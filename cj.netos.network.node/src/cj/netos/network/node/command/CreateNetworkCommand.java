package cj.netos.network.node.command;

import cj.netos.network.Castmode;
import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.IPrincipal;
import cj.netos.network.NetworkFrame;
import cj.netos.network.node.INetworkCommand;
import cj.netos.network.node.INetworkContainer;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.util.StringUtil;
import io.netty.channel.Channel;

public class CreateNetworkCommand implements INetworkCommand {
    INetworkContainer networkContainer;

    public CreateNetworkCommand(INetworkServiceProvider site) {
        networkContainer = (INetworkContainer) site.getService("$.network.networkContainer");
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
        Castmode frontendCastmode = null;
        if (StringUtil.isEmpty(str_frontendCastmode)) {
            frontendCastmode = Castmode.selectcast;
        } else {
            frontendCastmode = Castmode.valueOf(str_frontendCastmode);
        }

        String str_backendCastmode = frame.parameter("backendCastmode");
        Castmode backendCastmode = null;
        if (StringUtil.isEmpty(str_backendCastmode)) {
            backendCastmode = Castmode.unicast;
        } else {
            backendCastmode = Castmode.valueOf(str_backendCastmode);
        }
        networkContainer.createNetwork(principal, name, title, frontendCastmode, backendCastmode);
    }

    @Override
    public String getInstruction() {
        return "createNetwork";
    }

}
