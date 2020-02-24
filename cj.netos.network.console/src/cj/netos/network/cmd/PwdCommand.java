package cj.netos.network.cmd;

import cj.netos.network.CmdLine;
import cj.netos.network.Command;
import cj.netos.network.peer.IPeer;
import cj.ultimate.util.StringUtil;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.IOException;

public class PwdCommand extends Command {
    @Override
    public String cmd() {
        return "pwd";
    }

    @Override
    public String cmdDesc() {
        return "以账号密码登录";
    }

    @Override
    public Options options() {
        Options options = new Options();
        Option n = new Option("n", "peer", true, "[必须]自定义本地名");
        options.addOption(n);
        Option u = new Option("u", "person", true, "[必须]uc中心公号格式：cj@gbera.netos");
        options.addOption(u);
        Option p = new Option("p", "pwd", true, "[必须]密码");
        options.addOption(p);
        return options;
    }

    @Override
    public boolean doCommand(CmdLine cl) throws IOException {
        IPeer peer = cl.peer();
        CommandLine line = cl.line();
        String peerName = line.getOptionValue("n");
        if (StringUtil.isEmpty(peerName)) {
            System.out.println("缺少参数-n");
            return true;
        }
        String person = line.getOptionValue("u");
        if (StringUtil.isEmpty(person)) {
            System.out.println("缺少参数-u");
            return true;
        }
        String pwd = line.getOptionValue("p");
        if (StringUtil.isEmpty(pwd)) {
            System.out.println("缺少参数-p");
            return true;
        }
        peer.authByPassword(peerName, person, pwd);
        return true;
    }
}
