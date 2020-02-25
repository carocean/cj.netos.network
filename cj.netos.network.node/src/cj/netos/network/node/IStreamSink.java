package cj.netos.network.node;

import cj.netos.network.NetworkFrame;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.IClosable;

public interface IStreamSink extends IClosable {
    IStreamSink write(NetworkFrame frame) throws CircuitException;
    NetworkFrame pullFirst()throws CircuitException;
    IStreamSink removeFirst()throws CircuitException;
}
