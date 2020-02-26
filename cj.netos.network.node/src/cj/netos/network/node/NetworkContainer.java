package cj.netos.network.node;

import cj.netos.network.BackendCastmode;
import cj.netos.network.FrontendCastmode;
import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.IPrincipal;
import cj.studio.ecm.net.CircuitException;
import org.apache.jdbm.DB;
import org.apache.jdbm.DBMaker;

import java.io.File;
import java.util.Map;

public class NetworkContainer implements INetworkContainer {
    DB db;
    private Map<String, byte[]> infoMap;
    private NetworkConfig config;
    private String home;

    @Override
    public boolean isAutoCreate() {
        return config.isAutoCreate;
    }

    @Override
    public void close() {
        db.close();
    }

    @Override
    public void createNetwork(IPrincipal principal, String name, String title, FrontendCastmode frontendCastmode, BackendCastmode backendCastmode) throws CircuitException {
        NetworkInfo info = new NetworkInfo();
        info.name = name;
        info.title = title;
        info.frontendCastMode = frontendCastmode;
        info.backendCastmode = backendCastmode;
        byte[] b = info.toBytes();
        infoMap.put(name, b);
        db.commit();
    }

    @Override
    public INetwork openNetwork(String network) {
        byte[] b = infoMap.get(network);
        if (b == null) {
            return null;
        }
        NetworkInfo info = NetworkInfo.load(b);
        return new DefaultNetwork(info, home);
    }

    @Override
    public void removeNetwork(String network) throws CircuitException {
        infoMap.remove(network);
        db.commit();
    }

    @Override
    public void joinNetwork(String network, IPrincipal principal, boolean joinToFrontend) throws CircuitException {
        INetwork nw = openNetwork(network);
        if (nw == null) {
            throw new CircuitException("404", "网络不存在：" + network);
        }
        nw.removeMember(principal);
        nw.addMember(principal, joinToFrontend);
        nw.close();
    }

    @Override
    public void leaveNetwork(String network, IPrincipal principal) throws CircuitException {
        INetwork nw = openNetwork(network);
        if (nw == null) {
            return;
        }
        nw.removeMember(principal);
        nw.close();
    }

    @Override
    public void load(INetworkServiceProvider site, NetworkConfig config, String home) {
        this.home = home;
        this.config = config;
        String path = String.format("%s%sstore%snetworks%sinfos", home, File.separator, File.separator, File.separator);
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        path = String.format("%s%sdb_", path, File.separator);
        db = DBMaker.openFile(path).disableLocking().closeOnExit().make();
        Map<String, byte[]> infoMap = db.getHashMap("infos");
        if (infoMap == null) {
            infoMap = db.createHashMap("infos");
            db.commit();
        }
        this.infoMap = infoMap;
        Map<String, NetworkInfo> configNetworks = config.getNetworks();
        for (Map.Entry<String, NetworkInfo> entry : configNetworks.entrySet()) {
            byte[] b = infoMap.get(entry.getKey());
            if (b != null) {
                infoMap.remove(entry.getKey());
            }
            infoMap.put(entry.getKey(), entry.getValue().toBytes());
        }
        db.commit();

    }

    @Override
    public String[] enumNetwork() {
        return infoMap.keySet().toArray(new String[0]);
    }

    @Override
    public boolean containsNetwork(String name) {
        return infoMap.containsKey(name);
    }


}
