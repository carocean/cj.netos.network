package cj.netos.network.node.pipeline.command;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.IPrincipal;
import cj.netos.network.NetworkFrame;
import cj.netos.network.node.IEndportContainer;
import cj.netos.network.node.INetworkCommand;
import cj.netos.network.node.INetworkContainer;
import cj.studio.ecm.net.CircuitException;
import io.netty.channel.Channel;

public class LeaveNetworkCommand implements INetworkCommand {

    private final INetworkContainer networkContainer;

    public LeaveNetworkCommand(INetworkServiceProvider site) {
        networkContainer = (INetworkContainer) site.getService("$.network.networkContainer");
    }

    @Override
    public void exec(NetworkFrame frame, IPrincipal principal, Channel channel) throws CircuitException {
        networkContainer.leaveNetwork(frame.rootName(), principal);
    }

    @Override
    public String getInstruction() {
        return "leaveNetwork";
    }

}
