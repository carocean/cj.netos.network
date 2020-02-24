package cj.netos.network.node.command;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.IPrincipal;
import cj.netos.network.NetworkFrame;
import cj.netos.network.Sender;
import cj.netos.network.node.IEndpointer;
import cj.netos.network.node.IEndpointerContainer;
import cj.netos.network.node.INetworkCommand;
import cj.netos.network.node.INetworkContainer;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.util.StringUtil;
import io.netty.channel.Channel;

public class RemoveNetworkCommand extends AbastractNetworkCommand implements INetworkCommand {
    private final IEndpointerContainer endpointerContainer;
    INetworkContainer networkContainer;

    public RemoveNetworkCommand(INetworkServiceProvider site) {
        networkContainer = (INetworkContainer) site.getService("$.network.networkContainer");
        endpointerContainer = (IEndpointerContainer) site.getService("$.network.endpointerContainer");
    }

    @Override
    public void exec(NetworkFrame frame, IPrincipal principal, Channel channel) throws CircuitException {
        String name = frame.rootName();
        if (StringUtil.isEmpty(name)) {
            throw new CircuitException("500", "请求地址错误，未能确定访问的网络");
        }
        networkContainer.removeNetwork(principal, name);


        NetworkFrame back = new NetworkFrame("removeNetwork /system/notify/ network/1.0");
        if (principal != null) {
            back.head("sender-person", principal.principal());
            back.head("sender-peer", principal.peer());
        }
        back.head("sender-network", name);
        write(channel, back);

        NetworkFrame f = new NetworkFrame(String.format("removeNetwork /%s/notify/ network/1.0", networkContainer.getEventNetwork()));
        IEndpointer endpointer = endpointerContainer.endpoint(principal.key());
        endpointer.upstream(principal, f);
    }

    @Override
    public String getInstruction() {
        return "removeNetwork";
    }

}
