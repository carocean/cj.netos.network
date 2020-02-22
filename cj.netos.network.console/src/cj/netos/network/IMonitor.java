package cj.netos.network;

import cj.netos.network.peer.IPeer;
import cj.studio.ecm.IServiceProvider;
import org.apache.commons.cli.ParseException;

import java.io.IOException;

public interface IMonitor {
    void moniter(IPeer peer) throws ParseException, IOException;

}
