package cj.netos.network;

import cj.studio.ecm.net.CircuitException;

public interface IValve {
	void flow(NetworkFrame frame, IPipeline pipeline)throws CircuitException;

    void nextError(NetworkFrame frame, Throwable error, IPipeline pipeline)throws CircuitException;
}
