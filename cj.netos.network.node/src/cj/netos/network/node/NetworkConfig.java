package cj.netos.network.node;

import cj.studio.ecm.EcmException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkConfig {
    boolean isAutoCreate;
    String eventNetwork;
    Map<String, NetworkInfo> networks;

    public void parse(Map<String, Object> node) {
        networks = new HashMap<>();
        isAutoCreate = (boolean) node.get("isAutoCreate");
        eventNetwork = (String) node.get("eventNetwork");
        List<Object> list = (List) node.get("netowrks");
        for (Object obj : list) {
            Map<String, Object> item = (Map<String, Object>) obj;
            NetworkInfo info = new NetworkInfo();
            info.parse(item);
            if (networks.containsKey(info.getName())) {
                throw new EcmException("已存在网络：" + info.getName());
            }
            networks.put(info.getName(), info);
        }
    }

    public boolean isAutoCreate() {
        return isAutoCreate;
    }

    public String getEventNetwork() {
        return eventNetwork;
    }

    public Map<String, NetworkInfo> getNetworks() {
        return networks;
    }
}
