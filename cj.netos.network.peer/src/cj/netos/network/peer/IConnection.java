package cj.netos.network.peer;


import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.NetworkFrame;

import java.util.Map;

public interface IConnection{
    String getHost();

    String getProtocol();

    int getPort();

    void connect(String protocol, String ip, int port, Map<String, String> props);

    void close();

    void send(NetworkFrame frame);

    boolean isConnected();


    /**
     * 禁止重连。原因或许是peer认证失败了
     */
    void forbiddenReconnect();


}
