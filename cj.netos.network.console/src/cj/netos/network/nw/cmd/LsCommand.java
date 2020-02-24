package cj.netos.network.nw.cmd;

import cj.netos.network.CmdLine;
import cj.netos.network.Command;
import cj.netos.network.peer.ILogicNetwork;
import cj.netos.network.peer.IPeer;
import cj.studio.ecm.net.CircuitException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.IOException;
import java.util.Set;

public class LsCommand extends Command {
    @Override
    public String cmd() {
        return "ls";
    }

    @Override
    public String cmdDesc() {
        return "查看网络信息";
    }

    @Override
    public Options options() {
        Options options = new Options();
//        Option b = new Option("l", "local", false, "[可选]是否仅列出本地已侦听的网络");
//        options.addOption(b);
        return options;
    }

    @Override
    public boolean doCommand(CmdLine cl) throws IOException {
        ILogicNetwork network = cl.network();
        try {
            network.ls();
        } catch (CircuitException e) {
            e.printStackTrace();
            return true;
        }
        return true;
    }
}
