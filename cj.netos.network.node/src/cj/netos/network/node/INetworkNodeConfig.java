package cj.netos.network.node;

import java.io.FileNotFoundException;

public interface INetworkNodeConfig {
    void load(String home) throws FileNotFoundException;

    ServerInfo getServerInfo();

    String home();

    NetworkConfig getNetworkConfig();

    PumpInfo getPumpInfo();
}
