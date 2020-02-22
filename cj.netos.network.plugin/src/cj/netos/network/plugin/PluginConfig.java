package cj.netos.network.plugin;

import java.util.HashMap;
import java.util.Map;

public class PluginConfig {
    String appid;
    String appKey;
    String appSecret;
    Map<String, String> ports;
    long writeTimeout;
    private long readTimeout;
    private long connectTimeout;

    public long writeTimeout() {
        return writeTimeout;
    }


    public long readTimeout() {
        return readTimeout;
    }

    public long connectTimeout() {
        return connectTimeout;
    }

    public String appid() {
        return appid;
    }

    public String appKey() {
        return appKey;
    }

    public String appSecret() {
        return appSecret;
    }

    public Map<String, String> ports() {
        return ports;
    }

    public void parse(Map<String, Object> node) {
        appid = node.get("appid") + "";
        appKey = node.get("appKey") + "";
        appSecret = node.get("appSecret") + "";
        this.ports = new HashMap<>();
        Map<String, Object> ports = (Map<String, Object>) node.get("ports");
        for (String key : ports.keySet()) {
            this.ports.put(key, ports.get(key) + "");
        }
        Map<String, Object> okhttp = (Map<String, Object>) node.get("okhttp");
        readTimeout = Long.valueOf(okhttp.get("readTimeout") + "");
        connectTimeout = Long.valueOf(okhttp.get("connectTimeout") + "");
        writeTimeout = Long.valueOf(okhttp.get("writeTimeout") + "");
    }
}
