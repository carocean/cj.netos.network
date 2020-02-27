package cj.netos.network.peer;

import java.util.Map;

public interface IOnreconnection {
    void onreconnected(String protocol, String host, int port, Map<String, String> props);
}
