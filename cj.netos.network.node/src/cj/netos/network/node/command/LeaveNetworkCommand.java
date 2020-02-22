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

public class LeaveNetworkCommand implements INetworkCommand {
    INetworkContainer networkContainer;
    IEndpointerContainer endpointerContainer;

    public LeaveNetworkCommand(INetworkServiceProvider site) {
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
        String _isjoinToFrontend = frame.parameter("isJoinToFrontend");
        if (StringUtil.isEmpty(_isjoinToFrontend)) {
            _isjoinToFrontend = "true";
        }
        boolean joinToFrontend = Boolean.valueOf(_isjoinToFrontend);
        networkContainer.leaveNetwork(principal, name, joinToFrontend);
        endpointerContainer.onLeaveNetwork(principal, name);
    }

    @Override
    public String getInstruction() {
        return "leaveNetwork";
    }
}
