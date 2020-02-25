package cj.netos.network.node;


import cj.studio.ecm.net.CircuitException;

import java.io.Closeable;

public interface IEndport {
    IStreamSink openUpstream() throws CircuitException;

    IStreamSink openDownstream() throws CircuitException;
}
