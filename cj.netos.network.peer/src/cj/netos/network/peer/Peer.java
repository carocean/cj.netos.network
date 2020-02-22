package cj.netos.network.peer;

import cj.studio.ecm.EcmException;
import cj.ultimate.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

public class Peer implements IPeer {
    IConnection connection;

    private Peer() {
    }

    public static IPeer connect(String url) {
        int pos = url.indexOf("://");
        if (pos < 0) {
            throw new EcmException("地址格式错误:" + url);
        }
        String protocol = url.substring(0, pos);
        String remain = url.substring(pos + "://".length(), url.length());
        pos = remain.indexOf(":");
        int port = 0;
        String ip = "";
        Map<String, String> props = new HashMap<>();
        if (pos < 0) {
            ip = remain;
            port = 80;
        } else {
            ip = remain.substring(0, pos);
            remain = remain.substring(pos + 1, remain.length());
            pos = remain.indexOf("?");
            if (pos < 0) {
                String strPort = remain;
                port = Integer.valueOf(strPort);
            } else {
                String strPort = remain.substring(0, pos);
                port = Integer.valueOf(strPort);
                remain = remain.substring(pos + 1, remain.length());
                parseProps(remain, props);
            }
        }
        IConnection connection = new TcpConnection();
        connection.connect(protocol, ip, port, props);
        Peer peer=new Peer();
        peer.connection=connection;
        return peer;
    }

    private static void parseProps(String queryString, Map<String, String> props) {
        String[] arr = queryString.split("&");
        for (String pair : arr) {
            if (StringUtil.isEmpty(pair)) {
                continue;
            }
            String[] e = pair.split("=");
            String key = e[0];
            String v = "";
            if (e.length > 1) {
                v = e[1];
            }
            props.put(key, v);
        }
    }
    @Override
    public void authByPassword(String peer, String person, String password) {

    }

    @Override
    public void authByAccessToken(String accessToken) {

    }

    @Override
    public void listEventNetwork(IOnerror onerror, IOnopen onopen, IOnmessage onmessage, IOnclose onclose) {

    }

    @Override
    public ILogicNetwork listen(String networkName) {
        return null;
    }

    @Override
    public void close() {

    }

    @Override
    public String peerName() {
        return null;
    }
}
