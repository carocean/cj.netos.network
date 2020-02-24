package cj.netos.network.cmd;

import cj.netos.network.BackendCastmode;
import cj.netos.network.FrontendCastmode;
import cj.netos.network.CmdLine;
import cj.netos.network.Command;
import cj.netos.network.peer.IPeer;
import cj.ultimate.util.StringUtil;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.IOException;
import java.util.List;

public class CreateCommand extends Command {
    @Override
    public String cmd() {
        return "create";
    }

    @Override
    public String cmdDesc() {
        return "创建网络。例：create mynetwork -t '我的网络' ";
    }

    @Override
    public Options options() {
        Options options = new Options();
        Option t = new Option("t", "title", true, "[必选]网络显示名");
        options.addOption(t);
        Option b = new Option("b", "backendCastmode", true, "[可选]后置传播模式(unicast, multicast, selectcast, frontendcast)，默认是unicast");
        options.addOption(b);
        Option f = new Option("f", "frontendCastmode", true, "[可选]前置传播模式(unicast, multicast, selectcast)，默认是multicast");
        options.addOption(f);
        return options;
    }

    @Override
    public boolean doCommand(CmdLine cl) throws IOException {
        IPeer peer = cl.peer();
        CommandLine line = cl.line();
        List<String> args = line.getArgList();
        if (args.isEmpty()) {
            System.out.println(String.format("错误：未指定网络名"));
            return true;
        }
        String name = args.get(0);
        String title = line.getOptionValue("t");
        if (StringUtil.isEmpty(title)) {
            System.out.println(String.format("错误：缺少-t"));
            return true;
        }
        String backendCastmode = line.getOptionValue("b");
        if (StringUtil.isEmpty(backendCastmode)) {
            backendCastmode = "unicast";
        }
        String frontendCastmode = line.getOptionValue("f");
        if (StringUtil.isEmpty(frontendCastmode)) {
            frontendCastmode = "multicast";
        }
        peer.createNetwork(name, title, FrontendCastmode.valueOf(frontendCastmode), BackendCastmode.valueOf(backendCastmode));
        return true;
    }
}
