package cj.netos.network.cmd;

import cj.netos.network.CmdLine;
import cj.netos.network.Command;
import cj.netos.network.peer.IPeer;
import cj.ultimate.util.StringUtil;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.IOException;

public class TokenCommand extends Command {
    @Override
    public String cmd() {
        return "token";
    }

    @Override
    public String cmdDesc() {
        return "以访问令牌登录";
    }

    @Override
    public Options options() {
        Options options = new Options();
        Option t = new Option("t", "token", true, "[必须]访问令牌");
        options.addOption(t);
        return options;
    }

    @Override
    public boolean doCommand(CmdLine cl) throws IOException {
        IPeer peer = cl.peer();
        CommandLine line = cl.line();
        String token = line.getOptionValue("t");
        if (StringUtil.isEmpty(token)) {
            System.out.println("缺少参数-t");
            return true;
        }
        peer.authByAccessToken(token);
        return true;
    }
}
