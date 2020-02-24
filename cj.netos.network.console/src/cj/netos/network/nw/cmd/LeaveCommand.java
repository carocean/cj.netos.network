package cj.netos.network.nw.cmd;

import cj.netos.network.CmdLine;
import cj.netos.network.Command;
import cj.netos.network.peer.ILogicNetwork;
import cj.studio.ecm.net.CircuitException;
import org.apache.commons.cli.Options;

import java.io.IOException;

public class LeaveCommand extends Command {
    @Override
    public String cmd() {
        return "leave";
    }

    @Override
    public String cmdDesc() {
        return "离开网络";
    }

    @Override
    public Options options() {
        Options options = new Options();
        return options;
    }

    @Override
    public boolean doCommand(CmdLine cl) throws IOException {
        ILogicNetwork network = cl.network();
        try {
            network.leave();
            cl.peer().removeLogicNetwork(network);
        } catch (CircuitException e) {
            e.printStackTrace();
            return true;
        }
        return true;
    }
}
