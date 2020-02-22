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

public class AuthNotifyNetworkCommand implements INetworkCommand {
    INetworkContainer networkContainer;
    private IEndpointerContainer endpointerContainer;
    public AuthNotifyNetworkCommand(INetworkServiceProvider site) {
        networkContainer = (INetworkContainer) site.getService("$.network.networkContainer");
        endpointerContainer = (IEndpointerContainer) site.getService("$.network.endpointerContainer");
    }

    @Override
    public void exec(NetworkFrame frame, IPrincipal principal, Channel channel) throws CircuitException {
        endpointerContainer.openEndpoint(principal, channel);
        networkContainer.onnofity(frame, principal);
    }

    @Override
    public String getInstruction() {
        return "auth";
    }

}
