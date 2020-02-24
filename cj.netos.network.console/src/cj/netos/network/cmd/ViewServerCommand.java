package cj.netos.network.cmd;

import cj.netos.network.CmdLine;
import cj.netos.network.Command;
import cj.netos.network.peer.IPeer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.IOException;
import java.util.Set;

public class ViewServerCommand extends Command {
    @Override
    public String cmd() {
        return "vs";
    }

    @Override
    public String cmdDesc() {
        return "查看服务器信息，主要是ip地址";
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
        IPeer peer = cl.peer();
        peer.viewServer();
        return true;
    }
}
