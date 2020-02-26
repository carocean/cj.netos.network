package cj.netos.network.node.pump;

import cj.netos.network.NetworkFrame;
import cj.netos.network.node.*;
import cj.netos.network.node.eventloop.EventTask;
import cj.netos.network.node.eventloop.ILine;
import cj.netos.network.node.eventloop.IReceiver;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.gson2.com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

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
        IStreamSink sink = endport.openDownstream(task.getNetwork());
        while (true) {
            NetworkFrame frame = sink.pullFirst();
            if (frame == null) {
                break;
            }
            //终结点必须侦听了该网络才发
            if (!endpointer.hasListenNetwork(frame.rootName())) {
                break;
            }
            endpointer.write(frame);
            sink.removeFirst();
        }
        sink.close();
    }

    @Override
    public void error(EventTask task, Throwable error, ILine line) {
        IEndpointerContainer endpointerContainer = (IEndpointerContainer) line.site().getService("$.network.endpointerContainer");
        IEndpointer endpointer = endpointerContainer.endpointer(task.getEndpointKey());
        ByteBuf bb = Unpooled.buffer();
        Map<String, Object> map = new HashMap<>();
        StringWriter buffer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(buffer);
        error.printStackTrace(printWriter);
        map.put("cause", buffer.toString());
        bb.writeBytes(new Gson().toJson(map).getBytes());

        NetworkFrame back = new NetworkFrame(String.format("error /%s network/1.0",task.getNetwork()), bb);
        String key = task.getEndpointKey();
        int pos = key.indexOf("/");
        String person = key.substring(0, pos);
        String peer = key.substring(pos + 1);
        back.head("sender-person", person);
        back.head("sender-peer", peer);
        CircuitException ce = CircuitException.search(error);
        if (ce != null) {
            back.head("status", ce.getStatus());
            back.head("message", ce.getMessage() + "");
        } else {
            back.head("status", "500");
            back.head("message", error.getMessage() + "");
        }
        endpointer.write(back);
    }

    public SendFrameToEndpointer(IEndpointerContainer endpointerContainer, IEndportContainer endportContainer) {
        this.endpointerContainer = endpointerContainer;
        this.endportContainer = endportContainer;
    }
}
