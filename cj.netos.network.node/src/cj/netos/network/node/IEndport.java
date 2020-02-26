package cj.netos.network.node;


import cj.netos.network.ListenMode;
import cj.studio.ecm.net.CircuitException;

import java.io.Closeable;

public interface IEndport {
    EndportInfo getInfo();


    IStreamSink openUpstream(String network) throws CircuitException;

    IStreamSink openDownstream(String network) throws CircuitException;

    boolean isListenMode(String name, ListenMode upstream);

}
