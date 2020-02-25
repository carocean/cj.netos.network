package cj.netos.network.node.pump;

import cj.netos.network.NetworkFrame;
import cj.netos.network.node.*;
import cj.netos.network.node.eventloop.ILine;
import cj.netos.network.node.eventloop.IReceiver;
import cj.netos.network.node.eventloop.EventTask;
import cj.studio.ecm.net.CircuitException;

public class SendFrameToEndpointer implements IReceiver {
    private final IEndportContainer endportContainer;
    private final IEndpointerContainer endpointerContainer;

    @Override
    public void receive(EventTask task, ILine line) throws CircuitException {
        //如果不在线则不发
        if (!endpointerContainer.hasEndpointer(task.getEndpointKey())) {
            return;
        }
        IEndpointer endpointer = endpointerContainer.endpointer(task.getEndpointKey());
        IEndport endport = endportContainer.openport(task.getEndpointKey());
        IStreamSink sink = endport.openDownstream();
        while (true) {
            NetworkFrame frame = sink.pullFirst();
            if (frame == null) {
                break;
            }
            endpointer.write(frame);
            sink.removeFirst();
        }
        sink.close();
    }

    @Override
    public void error(EventTask task, Throwable e, ILine line) {

    }

    public SendFrameToEndpointer(IEndpointerContainer endpointerContainer, IEndportContainer endportContainer) {
        this.endpointerContainer = endpointerContainer;
        this.endportContainer = endportContainer;
    }
}
