package cj.netos.network.cmd;

import cj.netos.network.CmdLine;
import cj.netos.network.Command;
import cj.netos.network.IMonitor;
import cj.netos.network.nw.NetworkMonitor;
import cj.netos.network.peer.ILogicNetwork;
import cj.netos.network.peer.IPeer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class CdCommand extends Command {
    @Override
    public String cmd() {
        return "cd";
    }

    @Override
    public String cmdDesc() {
        return "再次进入已侦听的网络。例：cd mynetwork";
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
        ILogicNetwork logicNetwork = peer.localNetwork(name);
        if (logicNetwork == null) {
            System.out.println(String.format("错误：未侦听过该网络:" + name));
            return true;
        }
        Scanner scanner = new Scanner(System.in);
        IMonitor console = new NetworkMonitor(scanner, logicNetwork);
        try {
            console.moniter(cl.peer(), logicNetwork);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return true;
    }
}
