package cj.netos.network.nw;


import cj.netos.network.BaseMonitor;
import cj.netos.network.Command;
import cj.netos.network.IMonitor;
import cj.netos.network.nw.cmd.LeaveCommand;
import cj.netos.network.nw.cmd.LsCommand;
import cj.netos.network.nw.cmd.SendCommand;
import cj.netos.network.peer.ILogicNetwork;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class NetworkMonitor extends BaseMonitor implements IMonitor {
    private final ILogicNetwork logicNetwork;
    Scanner scanner;


    public NetworkMonitor(Scanner scanner, ILogicNetwork logicNetwork) {
        this.scanner = scanner;
        this.logicNetwork = logicNetwork;
    }

    @Override
    protected String getPrefix() {
        return logicNetwork.getNetwork() + ">";
    }

    @Override
    protected Map<String, Command> getCommands() {
        Map<String, Command> cmds = new HashMap<>();
        Command ls = new LsCommand();
        cmds.put(ls.cmd(), ls);
        Command leave = new LeaveCommand();
        cmds.put(leave.cmd(), leave);
        Command send = new SendCommand();
        cmds.put(send.cmd(), send);
        return cmds;
    }

    @Override
    protected Scanner getScanner() {
        return scanner;
    }

    @Override
    protected boolean isExit(String text) {
        return (text.startsWith("cd") && text.trim().endsWith(".."));
    }

    @Override
    protected boolean checkExitOnAfterCommand(String text) {
        return "leave".equals(text);
    }
}
