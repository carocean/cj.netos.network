package cj.netos.network.node;

import cj.netos.network.IPrincipal;
import cj.studio.ecm.net.CircuitException;
import io.netty.channel.Channel;

public interface IEndpointerContainer {
    void onChannelInactive(Channel channel) throws CircuitException;

    IEndpointer openEndpoint(IPrincipal principal, Channel channel) throws CircuitException;


    void onJoinNetwork(IPrincipal principal, String network);

    void onLeaveNetwork(IPrincipal principal, String network);

    IEndpointer endpoint(String key);

    ISinkPull createSinkPuller(String endpoint, String network);

    IEndpointer availableEndpoint();

}
