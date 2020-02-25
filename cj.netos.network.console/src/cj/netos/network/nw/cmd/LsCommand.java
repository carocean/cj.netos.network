package cj.netos.network.nw.cmd;

import cj.netos.network.CmdLine;
import cj.netos.network.Command;
import cj.netos.network.peer.ILogicNetwork;
import cj.netos.network.peer.IPeer;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.util.StringUtil;
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
        Option b = new Option("b", "backend", false, "[可选]列出后置成员");
        options.addOption(b);
        Option f = new Option("f", "frontend", false, "[可选]列出前置成员");
        options.addOption(f);
        return options;
    }

    @Override
    public boolean doCommand(CmdLine cl) throws IOException {
        String memberIn = "";
        if (cl.line().hasOption("b")) {
            memberIn = "backend";
        }
        if (cl.line().hasOption("f")) {
            memberIn = "frontend";
        }
        ILogicNetwork network = cl.network();
        try {
            network.ls(memberIn);
        } catch (CircuitException e) {
            e.printStackTrace();
            return true;
        }
        return true;
    }
}
