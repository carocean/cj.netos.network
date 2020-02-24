package cj.netos.network.node.command;

import cj.netos.network.NetworkFrame;
import cj.netos.network.node.ChannelWriter;
import cj.netos.network.node.INetworkCommand;
import io.netty.channel.Channel;

public abstract class AbastractNetworkCommand extends ChannelWriter implements INetworkCommand {
    @Override
    public void write(Channel channel, NetworkFrame frame) {
        if (!frame.containsHead("status")) {
            frame.head("status", "200");
        }
        if (!frame.containsHead("message")) {
            frame.head("message", "OK");
        }
        super.write(channel, frame);
    }
}
