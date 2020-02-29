package cj.netos.network.peer;

import cj.netos.network.BackendCastmode;
import cj.netos.network.FrontendCastmode;
import cj.netos.network.ListenMode;
import cj.netos.network.NetworkFrame;
import cj.studio.ecm.EcmException;
import cj.ultimate.util.StringUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Peer implements IPeer {
    IConnection connection;
    boolean isAuthed = false;
    private IOnreconnection onreconnection;

    private Peer() {
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

    public static IPeer connect(String url) {
        return connect(url, null, null,null,null);
    }

    public static IPeer connect(String url, IOnopen onopen, IOnclose onclose,IOnreconnection onreconnection,IOnnotify onnotify) {
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
        IConnection connection = null;
        switch (protocol) {
            case "tcp":
                connection = new TcpConnection(onopen, onclose,onnotify);
                connection.connect(protocol, ip, port, props);
                break;
            case "ws":
            case "wss":
                connection = new WSConnection(onopen, onclose,onnotify);
                connection.connect(protocol, ip, port, props);
                break;
            default:
                throw new EcmException("不支持的协议:" + protocol);
        }
        Peer peer = new Peer();
        peer.connection = connection;
        peer.onreconnection=onreconnection;
        return peer;
    }


    @Override
    public void authByPassword(String peer, String person, String password) {
        if (!connection.isForbiddenReconnect()) {
            connection.accept(new ReAuthByPassword(onreconnection,peer, person, password));
        }
        NetworkFrame frame = new NetworkFrame("auth / network/1.0");
        frame.head("auth-mode", "password");
        frame.parameter("peer", peer);
        frame.parameter("person", person);
        frame.parameter("password", password);
        connection.send(frame);
        isAuthed = true;
    }

    @Override
    public void authByAccessToken(String accessToken) {
        if (!connection.isForbiddenReconnect()) {
            connection.accept(new ReAuthByAccessToken(onreconnection,accessToken));
        }
        NetworkFrame frame = new NetworkFrame("auth / network/1.0");
        frame.head("auth-mode", "accessToken");
        frame.parameter("accessToken", accessToken);
        connection.send(frame);
        isAuthed = true;
    }

    void _checkAuthed() {
        if (isAuthed) {
            return;
        }
        throw new EcmException("未认证");
    }

    @Override
    public ILogicNetwork listen(String networkName, boolean isJoinToFrontend, ListenMode mode) {
        _checkAuthed();
        NetworkFrame frame = new NetworkFrame(String.format("listenNetwork /%s network/1.0", networkName));
        frame.parameter("isJoinToFrontend", isJoinToFrontend + "");
        frame.parameter("listenMode", mode.name());
        ILogicNetwork lnetwork = new DefaultLogicNetwork(networkName, connection);
        connection.addLogicNetwork(lnetwork);
        connection.send(frame);
        return lnetwork;
    }

    @Override
    public ILogicNetwork localNetwork(String networkName) {
        return connection.localNetwork(networkName);
    }

    @Override
    public Set<String> enumLocalNetwork() {
        return connection.enumLocalNetwork();
    }

    @Override
    public void removeLogicNetwork(ILogicNetwork network) {
        connection.removeLogicNetwork(network.getNetwork());
    }

    @Override
    public void createNetwork(String networkName, String title, FrontendCastmode frontendCastmode, BackendCastmode backendCastmode) {
        _checkAuthed();
        NetworkFrame frame = new NetworkFrame(String.format("createNetwork /%s network/1.0", networkName));
        frame.parameter("title", title);
        frame.parameter("frontendCastmode", frontendCastmode.name());
        frame.parameter("backendCastmode", backendCastmode.name());
        connection.send(frame);
    }

    @Override
    public void removeNetwork(String networkName) {
        _checkAuthed();
        NetworkFrame frame = new NetworkFrame(String.format("removeNetwork /%s network/1.0", networkName));
        connection.send(frame);
    }

    @Override
    public void listNetwork() {
        _checkAuthed();
        NetworkFrame frame = new NetworkFrame(String.format("listNetwork / network/1.0"));
        connection.send(frame);
    }

    @Override
    public void viewServer() {
        _checkAuthed();
        NetworkFrame frame = new NetworkFrame(String.format("viewServer / network/1.0"));
        connection.send(frame);
    }

    @Override
    public void close() {
        connection.close();
    }

    private class ReAuthByPassword implements IOnreconnection {

        private final String peer;
        private final String person;
        private final String password;
        IOnreconnection parent;
        public ReAuthByPassword(IOnreconnection parent,String peer, String person, String password) {
            this.peer = peer;
            this.person = person;
            this.password = password;
            this.parent=parent;
        }

        @Override
        public void onreconnected(String protocol, String host, int port, Map<String, String> props) {
            authByPassword(peer, person, password);
            if (parent != null) {
                parent.onreconnected(protocol,host,port,props);
            }
        }
    }

    private class ReAuthByAccessToken implements IOnreconnection {
        String accessToken;
        IOnreconnection parent;
        @Override
        public void onreconnected(String protocol, String host, int port, Map<String, String> props) {
            authByAccessToken(accessToken);
            if (parent != null) {
                parent.onreconnected(protocol,host,port,props);
            }
        }

        public ReAuthByAccessToken(IOnreconnection parent,String accessToken) {
            this.accessToken = accessToken;
            this.parent=parent;
        }
    }
}
