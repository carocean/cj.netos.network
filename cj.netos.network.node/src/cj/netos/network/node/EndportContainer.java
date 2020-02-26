package cj.netos.network.node;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.IPrincipal;
import cj.netos.network.ListenMode;
import cj.studio.ecm.net.CircuitException;
import org.apache.jdbm.DB;
import org.apache.jdbm.DBMaker;

import java.io.File;
import java.util.Map;

public class EndportContainer implements IEndportContainer ,IEndportInfoChanged{
    final static String KEY_MAP = "endports";
    private final INetworkServiceProvider site;
    String homeDir;
    Map<String, byte[]> endportInfoMap;
    DB db;

    public EndportContainer(INetworkServiceProvider site) {
        INetworkNodeConfig config = (INetworkNodeConfig) site.getService("$.network.config");
        homeDir = config.home();
        this.site = site;
        initDB();
    }

    private void initDB() {
        String dir = String.format("%s%sstore%sendports%sinfos", homeDir, File.separator, File.separator, File.separator);
        File fdir = new File(dir);
        if (!fdir.exists()) {
            fdir.mkdirs();
        }
        dir = String.format("%s%sdb_", dir, File.separator);
        db = DBMaker.openFile(dir).disableLocking().closeOnExit().make();
        Map<String, byte[]> map = db.getHashMap(KEY_MAP);
        if (map == null) {
            map = db.createHashMap(KEY_MAP);
        }
        endportInfoMap = map;
    }

    @Override
    public void close() {
        db.close();
    }

    @Override
    public IEndport openport(IPrincipal principal) throws CircuitException {
        return openport(principal.key());
    }

    @Override
    public IEndport openport(String principalKey) throws CircuitException {
        if (endportInfoMap.containsKey(principalKey)) {
            byte[] b = endportInfoMap.get(principalKey);
            EndportInfo info = EndportInfo.load(b,this);
            return new DefaultEndport(info, homeDir, site);
        }
        return _createEndport(principalKey);
    }

    private IEndport _createEndport(String principalKey) {
        EndportInfo info = new EndportInfo(this);
        info.principalKey = principalKey;
        endportInfoMap.put(info.principalKey, info.toBytes());
        db.commit();
        return new DefaultEndport(info, homeDir, site);
    }

    @Override
    public void addListenNetwork(EndportInfo info) {
        endportInfoMap.put(info.principalKey, info.toBytes());
        db.commit();
    }

    @Override
    public void removeNetworkListenmode(EndportInfo info) {
        endportInfoMap.remove(info.principalKey);
        db.commit();
    }
}
