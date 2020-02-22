package cj.netos.network.node;

import cj.netos.network.IPrincipal;
import cj.netos.network.NetworkFrame;
import cj.netos.network.node.IEndpointerContainer;
import cj.studio.ecm.net.CircuitException;
import io.netty.channel.Channel;

public interface INetworkCommand {
    void exec(NetworkFrame frame, IPrincipal principal, Channel channel) throws CircuitException;

    String getInstruction();

}
