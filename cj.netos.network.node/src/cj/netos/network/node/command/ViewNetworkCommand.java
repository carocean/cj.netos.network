package cj.netos.network.node.command;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.IPrincipal;
import cj.netos.network.NetworkFrame;
import cj.netos.network.node.IEndpointerContainer;
import cj.netos.network.node.INetworkCommand;
import cj.netos.network.node.INetworkContainer;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.util.StringUtil;
import io.netty.channel.Channel;

public class ViewNetworkCommand implements INetworkCommand {
    INetworkContainer networkContainer;
    public ViewNetworkCommand(INetworkServiceProvider site) {
        networkContainer = (INetworkContainer) site.getService("$.network.networkContainer");
    }
    @Override
    public void exec(NetworkFrame frame, IPrincipal principal, Channel channel) throws CircuitException {
        String name=frame.rootName();
        if (StringUtil.isEmpty(name)) {
            throw new CircuitException("500","请求地址错误，未能确定访问的网络");
        }
        networkContainer.viewNetwork(principal,name);
    }

    @Override
    public String getInstruction() {
        return "viewNetwork";
    }

}
