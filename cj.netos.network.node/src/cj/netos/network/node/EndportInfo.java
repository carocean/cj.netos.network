package cj.netos.network.node;

import cj.netos.network.ListenMode;
import cj.ultimate.gson2.com.google.gson.Gson;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EndportInfo {
    String principalKey;
    Map<String, ListenMode> networkListenmodeMap;
    transient IEndportInfoChanged endportInfoChanged;

    public EndportInfo(IEndportInfoChanged endportInfoChanged) {
        networkListenmodeMap = new ConcurrentHashMap<>();
        this.endportInfoChanged = endportInfoChanged;
    }

    public byte[] toBytes() {
        return new Gson().toJson(this).getBytes();
    }

    public static EndportInfo load(byte[] bytes, IEndportInfoChanged endportInfoChanged) {
        EndportInfo endportInfo = new Gson().fromJson(new String(bytes), EndportInfo.class);
        if (endportInfo.networkListenmodeMap == null) {
            endportInfo.networkListenmodeMap = new ConcurrentHashMap<>();
        }
        endportInfo.endportInfoChanged = endportInfoChanged;
        return endportInfo;
    }

    public String getPrincipalKey() {
        return principalKey;
    }

    public void addListenNetwork(String network, ListenMode mode) {
        networkListenmodeMap.put(network, mode);
        endportInfoChanged.addListenNetwork(this);
    }

    public void removeNetworkListenmode(String network) {
        networkListenmodeMap.remove(network);
        endportInfoChanged.removeNetworkListenmode(this);
    }

    public boolean containsNetworkListenmode(String network) {
        return networkListenmodeMap.containsKey(network);
    }

    public String[] enumNetworkListenmode() {
        return networkListenmodeMap.keySet().toArray(new String[0]);
    }

    public ListenMode getNetworkListenmode(String network) {
        return networkListenmodeMap.get(network);
    }
}