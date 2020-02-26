package cj.netos.network.node;

import cj.netos.network.IPrincipal;
import cj.netos.network.NetworkFrame;
import cj.studio.ecm.net.CircuitException;
import com.leansoft.bigqueue.BigQueueImpl;
import com.leansoft.bigqueue.IBigQueue;

import java.io.File;
import java.io.IOException;

public class DefaultStreamSink implements IStreamSink {
    IBigQueue queue;

    public DefaultStreamSink(String path,String network) throws CircuitException {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        try {
            queue = new BigQueueImpl(path, network);
        } catch (IOException e) {
            throw new CircuitException("404", e);
        }
    }
    @Override
    public void close() {
        try {
            queue.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized IStreamSink write( NetworkFrame frame) throws CircuitException {
        try {
            queue.enqueue(frame.toBytes());
            return this;
        } catch (IOException e) {
            throw new CircuitException("404", e);
        }
    }

    @Override
    public synchronized NetworkFrame pullFirst() throws CircuitException {
        try {
            byte[] b = queue.peek();
            if (b == null) {
                return null;
            }
            return new NetworkFrame(b);
        } catch (IOException e) {
            throw new CircuitException("404", e);
        }
    }

    @Override
    public synchronized IStreamSink removeFirst() throws CircuitException {
        try {
            queue.dequeue();
            return this;
        } catch (IOException e) {
            throw new CircuitException("404", e);
        }
    }

}
