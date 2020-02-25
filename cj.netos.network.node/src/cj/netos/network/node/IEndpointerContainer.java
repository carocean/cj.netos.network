package cj.netos.network.node;

import cj.netos.network.IPrincipal;
import cj.studio.ecm.net.CircuitException;
import io.netty.channel.Channel;

public interface IEndpointerContainer {


    IEndpointer createEndpointer(IPrincipal principal, Channel channel) throws CircuitException;

    IEndpointer endpointer(String endpointerKey);

    boolean hasEndpointer(String endpointerKey);

    void offline(Channel channel) throws CircuitException;

    void online(IPrincipal principal);

}
