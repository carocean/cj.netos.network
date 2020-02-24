package cj.netos.network.cmd;

import cj.netos.network.CmdLine;
import cj.netos.network.Command;
import cj.netos.network.peer.IPeer;
import cj.ultimate.util.StringUtil;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.IOException;
import java.util.List;

public class RemoveCommand extends Command {
    @Override
    public String cmd() {
        return "remove";
    }

    @Override
    public String cmdDesc() {
        return "移除网络。例：remove mynetwork";
    }

    @Override
    public Options options() {
        Options options = new Options();
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
        peer.removeNetwork(name);
        return true;
    }
}
