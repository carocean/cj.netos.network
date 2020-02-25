package cj.netos.network.node.pipeline.valve;

import cj.netos.network.*;
import cj.netos.network.node.IEndpointerContainer;
import cj.netos.network.node.INetworkCommand;
import cj.netos.network.node.command.*;
import cj.studio.ecm.net.CircuitException;
import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;

/**
 * 命令行服务：用于管道终结点、终结口、网络和水泵等等，只要是客户端想管道的都由本类实现
 */
public class DispatchCommandValve implements IValve {
    final String _PROTOCOL = "NETWORK/1.0";
    private IEndpointerContainer endpointerContainer;
    private Map<String, INetworkCommand> commands;
    ICheckRights checkRights;
    public DispatchCommandValve(INetworkServiceProvider site) {
        endpointerContainer = (IEndpointerContainer) site.getService("$.network.endpointerContainer");
        INetworkNodePlugin plugin = (INetworkNodePlugin) site.getService("$.network.plugin");
        checkRights=plugin.createCheckRights();
        commands = new HashMap<>();

        registerCommand(new AuthNotifyNetworkCommand(site));
        registerCommand(new ListenNetworkCommand(site));
        registerCommand(new LeaveNetworkCommand(site));
        registerCommand(new ListNetworkCommand(site));
        registerCommand(new CreateNetworkCommand(site));
        registerCommand(new RemoveNetworkCommand(site));
        registerCommand(new ViewNetworkCommand(site));
        registerCommand(new ViewServerCommand(site));
    }

    private void registerCommand(INetworkCommand command) {
        commands.put(command.getInstruction(), command);
    }

    @Override
    public void flow(NetworkFrame frame, IPipeline pipeline) throws CircuitException {
        if (pipeline.principal() == null) {
            CircuitException e = new CircuitException("404", "pipeline.principal为空，网络未知上线者");
            nextError(frame, e, pipeline);
            throw e;
        }
        if (!_PROTOCOL.equals(frame.protocol())) {
            pipeline.nextFlow(frame, this);
            return;
        }
        if (!commands.containsKey(frame.command())) {
            CircuitException e = new CircuitException("404", "不支持的系统命令：" + frame.command());
            nextError(frame, e, pipeline);
            throw e;
        }
        INetworkCommand command = commands.get(frame.command());
        IPrincipal principal = pipeline.principal();
        Channel channel = (Channel) pipeline.attachment();
        if (!checkRights.checkRights(command.getInstruction(), principal)) {
            CircuitException e = new CircuitException("803", "没有执行权限：" + frame.command());
            nextError(frame, e, pipeline);
//            channel.close();
            throw e;
        }
        command.exec(frame, principal, channel);
    }

    @Override
    public void nextError(NetworkFrame frame, Throwable error, IPipeline pipeline) throws CircuitException {
        pipeline.nextError(frame, error, this);
    }

}
