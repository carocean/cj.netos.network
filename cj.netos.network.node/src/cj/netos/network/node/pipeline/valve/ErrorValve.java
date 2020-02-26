package cj.netos.network.node.pipeline.valve;

import cj.netos.network.*;
import cj.netos.network.node.ChannelWriter;
import cj.netos.network.node.IEndpointer;
import cj.netos.network.node.IEndpointerContainer;
import cj.netos.network.node.INetworkContainer;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.gson2.com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class ErrorValve implements IValve {
    private final IEndpointerContainer endpointerContainer;
    private final INetworkContainer networkContainer;
    private final ChannelWriter channelWriter;

    public ErrorValve(INetworkServiceProvider site) {
        endpointerContainer = (IEndpointerContainer) site.getService("$.network.endpointerContainer");
        networkContainer = (INetworkContainer) site.getService("$.network.networkContainer");
        channelWriter = new ChannelWriter();
    }

    @Override
    public void flow(NetworkFrame frame, IPipeline pipeline) throws CircuitException {
        pipeline.nextFlow(frame, this);
    }

    @Override
    public void nextError(NetworkFrame frame, Throwable error, IPipeline pipeline) throws CircuitException {
        ByteBuf bb = Unpooled.buffer();
        Map<String, Object> map = new HashMap<>();
        StringWriter buffer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(buffer);
        error.printStackTrace(printWriter);
        map.put("cause", buffer.toString());
        bb.writeBytes(new Gson().toJson(map).getBytes());

        NetworkFrame back = new NetworkFrame(String.format("error / network/1.0"), bb);
        IPrincipal principal = pipeline.principal();
        if (principal != null) {
            back.head("sender-person", principal.principal());
            back.head("sender-peer", principal.peer());
        }
        CircuitException ce = CircuitException.search(error);
        if (ce != null) {
            back.head("status", ce.getStatus());
            back.head("message", ce.getMessage() + "");
        } else {
            back.head("status", "500");
            back.head("message", error.getMessage() + "");
        }
        Channel channel = (Channel) pipeline.attachment();
        channelWriter.write(channel, back);

    }


}
