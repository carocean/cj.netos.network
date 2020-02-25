package cj.netos.network.node.command;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.IPrincipal;
import cj.netos.network.NetworkFrame;
import cj.netos.network.node.ChannelWriter;
import cj.netos.network.node.INetworkCommand;
import cj.netos.network.node.INetworkContainer;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.util.StringUtil;
import io.netty.channel.Channel;

public class RemoveNetworkCommand implements INetworkCommand {
    private final INetworkContainer networkContainer;
    private final ChannelWriter channelWriter;

    @Override
    public void exec(NetworkFrame frame, IPrincipal principal, Channel channel) throws CircuitException {
        String name = frame.rootName();
        if (StringUtil.isEmpty(name)) {
            throw new CircuitException("500", "请求地址错误，未能确定访问的网络");
        }
        networkContainer.removeNetwork(name);

        NetworkFrame back = new NetworkFrame("removeNetwork /system/notify/ network/1.0");
        if (principal != null) {
            back.head("sender-person", principal.principal());
            back.head("sender-peer", principal.peer());
        }
        back.head("sender-network", name);
        channelWriter.write(channel, back);
    }

    @Override
    public String getInstruction() {
        return "removeNetwork";
    }

    public RemoveNetworkCommand(INetworkServiceProvider site) {
        networkContainer = (INetworkContainer) site.getService("$.network.networkContainer");
        channelWriter = new ChannelWriter();
    }
}
