package cj.netos.network;


import cj.netos.network.cmd.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class PeerMonitor extends BaseMonitor {

    @Override
    protected boolean isExit(String text) {
        return "bye".equals(text) || "exit".equals(text);
    }

    @Override
    protected boolean checkExitOnAfterCommand(String text) {
        return false;
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
        Command create = new CreateCommand();
        cmds.put(create.cmd(), create);
        Command remove = new RemoveCommand();
        cmds.put(remove.cmd(), remove);
        Command list = new ListCommand();
        cmds.put(list.cmd(), list);
        Command listen = new ListenCommand();
        cmds.put(listen.cmd(), listen);
        Command cd = new CdCommand();
        cmds.put(cd.cmd(), cd);
        Command vs = new ViewServerCommand();
        cmds.put(vs.cmd(), vs);
        return cmds;
    }
}
