package cj.netos.network.node;

import cj.netos.network.INetworkServiceProvider;
import cj.studio.ecm.net.CircuitException;
import com.leansoft.bigqueue.BigQueueImpl;
import com.leansoft.bigqueue.IBigQueue;

import java.io.File;
import java.io.IOException;

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
    public IStreamSink openUpstream() throws CircuitException {
        IBigQueue queue = null;
        try {
            queue = new BigQueueImpl(dir, "upstream");
        } catch (IOException e) {
            throw new CircuitException("404", e);
        }
        return new DefaultStreamSink(queue);
    }

    @Override
    public IStreamSink openDownstream() throws CircuitException {
        IBigQueue queue = null;
        try {
            queue = new BigQueueImpl(dir, "downstream");
        } catch (IOException e) {
            throw new CircuitException("404", e);
        }
        return new DefaultStreamSink(queue);
    }
}
