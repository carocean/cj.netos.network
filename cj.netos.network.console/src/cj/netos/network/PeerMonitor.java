package cj.netos.network;


import cj.netos.network.cmd.PwdCommand;
import cj.netos.network.cmd.TokenCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class PeerMonitor extends BaseMonitor {

    @Override
    protected boolean isExit(String text) {
        return "bye".equals(text) || "exit".equals(text);
    }

    @Override
    protected Scanner getScanner() {
        return new Scanner(System.in);
    }

    @Override
    protected String getPrefix() {
        return ">";
    }

    @Override
    protected Map<String, Command> getCommands() {
        Map<String, Command> cmds = new HashMap<>();
        Command pwd = new PwdCommand();
        cmds.put(pwd.cmd(), pwd);
        Command token = new TokenCommand();
        cmds.put(token.cmd(), token);
//        Command remove = new RemoveNetworkCommand();
//        cmds.put(remove.cmd(), remove);
//        Command exists = new ExistsNetworkCommand();
//        cmds.put(exists.cmd(), exists);
//        Command rename = new RenameNetworkCommand();
//        cmds.put(rename.cmd(), rename);
//        Command castmode = new CastmodeNetworkCommand();
//        cmds.put(castmode.cmd(), castmode);
//        Command listen = new ListenNetworkCommand(this.childMonitorController);
//        cmds.put(listen.cmd(), listen);
        return cmds;
    }
}
