package cj.netos.network.node.command;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.IPrincipal;
import cj.netos.network.NetworkFrame;
import cj.netos.network.node.IEndpointerContainer;
import cj.netos.network.node.INetworkCommand;
import cj.studio.ecm.net.CircuitException;
import io.netty.channel.Channel;

public class AuthNotifyNetworkCommand implements INetworkCommand {
    private IEndpointerContainer endpointerContainer;

    @Override
    public void exec(NetworkFrame frame, IPrincipal principal, Channel channel) throws CircuitException {
       if (endpointerContainer.hasEndpointer(principal.key())){
           return;
       }
        endpointerContainer.online(channel,principal);
    }

    @Override
    public String getInstruction() {
        return "auth";
    }

    public AuthNotifyNetworkCommand(INetworkServiceProvider site) {
        endpointerContainer = (IEndpointerContainer) site.getService("$.network.endpointerContainer");
    }
}
