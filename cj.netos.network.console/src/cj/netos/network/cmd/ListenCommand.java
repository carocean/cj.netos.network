package cj.netos.network.cmd;

import cj.netos.network.*;
import cj.netos.network.nw.NetworkMonitor;
import cj.netos.network.peer.ILogicNetwork;
import cj.netos.network.peer.IOnmessage;
import cj.netos.network.peer.IOnnotify;
import cj.netos.network.peer.IPeer;
import cj.ultimate.util.StringUtil;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class ListenCommand extends Command implements IOnmessage {
    @Override
    public String cmd() {
        return "listen";
    }

    @Override
    public String cmdDesc() {
        return "侦听网络。例：listen mynetwork -j backend:downstream。注：从网络窗口返回上级使用命令: cd ..";
    }

    @Override
    public Options options() {
        Options options = new Options();
        Option b = new Option("j", "join", true, "[可选]加入方式，格式是'前后:侦听模式'，默认是frontend:both。前为frontend，后为backend；侦听模式为：upstream｜downstream｜both，默认是both");
        options.addOption(b);
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
        String join = line.getOptionValue("j");
        if (StringUtil.isEmpty(join)) {
            join = "frontend:both";
        }
        int pos = join.indexOf(":");
        if (pos < 0) {
            System.out.println(String.format("错误参数：-j"));
            return true;
        }
        String prev = join.substring(0, pos);
        boolean isFrontend = true;
        switch (prev) {
            case "frontend":
                isFrontend = true;
                break;
            case "backend":
                isFrontend = false;
                break;
        }
        ListenMode _mode = ListenMode.valueOf(join.substring(pos + 1));
        ILogicNetwork logicNetwork = peer.listen(name, isFrontend, _mode);
        logicNetwork.onmessage(this);
        Scanner scanner = new Scanner(System.in);
        IMonitor console = new NetworkMonitor(scanner, logicNetwork);
        try {
            console.moniter(cl.peer(), logicNetwork);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void onmessage(ILogicNetwork logicNetwork, NetworkFrame frame) {
        System.out.println(String.format("--------------网络:%s的响应--------------------", logicNetwork.getNetwork()));
        System.out.println(new String(frame.toBytes()));
        System.out.println("--------------end");
    }
}
