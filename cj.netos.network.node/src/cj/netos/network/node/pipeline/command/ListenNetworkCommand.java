package cj.netos.network.node.pipeline.command;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.IPrincipal;
import cj.netos.network.ListenMode;
import cj.netos.network.NetworkFrame;
import cj.netos.network.node.IEndportContainer;
import cj.netos.network.node.INetworkCommand;
import cj.netos.network.node.INetworkContainer;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.util.StringUtil;
import io.netty.channel.Channel;

public class ListenNetworkCommand implements INetworkCommand {
    IEndportContainer endportContainer;
    INetworkContainer networkContainer;

    @Override
    public void exec(NetworkFrame frame, IPrincipal principal, Channel channel) throws CircuitException {
        String _listenmode = frame.parameter("listenMode");
        if (StringUtil.isEmpty(_listenmode)) {
            _listenmode = "both";
        }
        ListenMode mode = ListenMode.valueOf(_listenmode);
        endportContainer.openport(principal, mode);

        String _isjoinToFrontend = frame.parameter("isJoinToFrontend");
        if (StringUtil.isEmpty(_isjoinToFrontend)) {
            _isjoinToFrontend = "true";
        }
        boolean joinToFrontend = Boolean.valueOf(_isjoinToFrontend);
        networkContainer.joinNetwork(frame.rootName(), principal, joinToFrontend);
    }

    @Override
    public String getInstruction() {
        return "listenNetwork";
    }

    public ListenNetworkCommand(INetworkServiceProvider site) {
        networkContainer = (INetworkContainer) site.getService("$.network.networkContainer");
        endportContainer = (IEndportContainer) site.getService("$.network.endportContainer");
    }
}
