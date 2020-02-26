package cj.netos.network.node;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.ListenMode;
import cj.studio.ecm.net.CircuitException;

import java.io.File;

public class DefaultEndport implements IEndport {
    EndportInfo info;
    String dir;
    INetworkServiceProvider site;

    public DefaultEndport(EndportInfo info, String homeDir, INetworkServiceProvider site) {
        this.info = info;
        this.site = site;

        String dir = String.format("%s%sstore%sendports%s%s%s", homeDir, File.separator, File.separator, File.separator, info.principalKey, File.separator);
        File fdir = new File(dir);
        if (!fdir.exists()) {
            fdir.mkdirs();
        }
        this.dir = dir;
    }

    @Override
    public EndportInfo getInfo() {
        return info;
    }

    @Override
    public IStreamSink openUpstream(String network) throws CircuitException {
        String path = String.format("%supstream%s", dir, File.separator);
        return new DefaultStreamSink(path, network);
    }

    @Override
    public IStreamSink openDownstream(String network) throws CircuitException {
        String path = String.format("%sdownstream%s", dir, File.separator);
        return new DefaultStreamSink(path, network);
    }

    @Override
    public boolean isListenMode(String network, ListenMode listenMode) {
        if (!info.containsNetworkListenmode(network)) {
            return false;
        }
        ListenMode mode = this.info.getNetworkListenmode(network);
        return mode == listenMode;
    }
}
