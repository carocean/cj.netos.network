package cj.netos.network.node;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.Map;

public class NetworkNodeConfig implements INetworkNodeConfig {
    ServerInfo serverInfo;
    private String home;
    private PumpInfo pumpInfo;
    NetworkConfig networkConfig;

    @Override
    public void load(String home) throws FileNotFoundException {
        this.home = home;
        Yaml nodeyaml = new Yaml();
        String confNodeFile = String.format("%s%sconf%snode.yaml", home, File.separator, File.separator);
        Reader reader = new FileReader(confNodeFile);
        Map<String, Object> node = nodeyaml.load(reader);
        parseServerInfo(node);
        parsePumpInfo(node);
        parseNetowrks(node);
    }

    private void parseNetowrks(Map<String, Object> node) {
        networkConfig = new NetworkConfig();
        networkConfig.parse((Map<String, Object>) node.get("networkContainer"));
    }

    private void parsePumpInfo(Map<String, Object> node) {
        PumpInfo pumpInfo = new PumpInfo();
        pumpInfo.parse(node);
        this.pumpInfo = pumpInfo;
    }
    @Override
    public NetworkConfig getNetworkConfig() {
        return networkConfig;
    }

    @Override
    public PumpInfo getPumpInfo() {
        return this.pumpInfo;
    }

    @Override
    public String home() {
        return home;
    }

    @Override
    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    private void parseServerInfo(Map<String, Object> node) {
        serverInfo = new ServerInfo();
        serverInfo.parse(node);
    }

}
