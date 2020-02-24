package cj.netos.network.nw.cmd;

import cj.netos.network.CmdLine;
import cj.netos.network.Command;
import cj.netos.network.ConsoleEditor;
import cj.netos.network.NetworkFrame;
import cj.netos.network.peer.ILogicNetwork;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.util.StringUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.IOException;

public class SendCommand extends Command {
    @Override
    public String cmd() {
        return "send";
    }

    @Override
    public String cmdDesc() {
        return "发送消息";
    }

    @Override
    public Options options() {
        Options options = new Options();
        Option f = new Option("t", "times", true, "[可省略]发送次数,默认是1次");
        options.addOption(f);
        Option b = new Option("s", "sleep", true, "[可省略]每次发送时间间隔（毫秒");
        options.addOption(b);
        return options;
    }

    @Override
    public boolean doCommand(CmdLine cl) throws IOException {
        ILogicNetwork network = cl.network();
        CommandLine line = cl.line();
        StringBuffer sb = new StringBuffer();
        System.out.println(String.format("输入Frame，说明:"));
        System.out.println(String.format("\t- 第一行是请求头，如：put /path1/p2?xx=3&t=s my/1.0"));
        System.out.println(String.format("\t- 接着的行header，写法：key=value，然后回撤，其后接连多行均为header"));
        System.out.println(String.format("\t- 接着回撤一空行 为parameter，写法：key=value，然后回撤，其后接连多行均为parameter"));
        System.out.println(String.format("\t- 接着回撤两空行 为content，内容为任意输入"));
        System.out.println(String.format("\t- 如果无header且有参数则在请求行后回撤一个空行，如果有头有内容无参数，则在内容前回撤三个空行"));
        System.out.println(String.format("\t- 以!q号结输输入:"));
        //
        ConsoleEditor.readConsole("\t", "\r\n", ConsoleEditor.newReader(), sb);
        String text = sb.toString();
        if (StringUtil.isEmpty(text)) {
            return true;
        }
        while (text.startsWith("\r\n")) {
            text = text.substring(2, text.length());
        }

        int pos = text.indexOf("\r\n");
        String frameline = "";
        String frameText = "";
        if (pos < 0) {
            frameline = text;
        } else {
            frameline = text.substring(0, pos);
            frameText = text.substring(pos + 2, text.length());
        }

        ByteBuf bb = Unpooled.buffer();
        NetworkFrame frame = new NetworkFrame(frameline, bb);
        if (!StringUtil.isEmpty(frameText)) {
            byte[] raw = frameText.getBytes();
            NetworkFrame frame2 = new NetworkFrame(raw);
            frame.add(frame2);
            frame2.dispose();
        }
        long times = 1;
        if (line.hasOption("t")) {
            times = Long.valueOf(line.getOptionValue("t"));
        }
        if (times < 1) {
            times = 1;
        }
        long s = 0;
        if (line.hasOption("s")) {
            s = Long.valueOf(line.getOptionValue("s"));
        }
        for (int i = 0; i < times; i++) {
            System.out.println(String.format("sending %s", i));
            frame.head("Test-Counter", i + "");
            try {
                network.send(frame.copy());
            } catch (CircuitException e) {
                e.printStackTrace();
                break;
            }
            if (s > 0) {
                try {
                    Thread.sleep(s);
                } catch (InterruptedException e) {
                }
            }
        }
        frame.dispose();
        return true;
    }
}
