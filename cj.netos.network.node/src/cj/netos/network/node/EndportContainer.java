package cj.netos.network.node;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.IPrincipal;
import cj.netos.network.ListenMode;
import cj.studio.ecm.net.CircuitException;
import org.apache.jdbm.DB;
import org.apache.jdbm.DBMaker;

import java.io.File;
import java.util.Map;

public class EndportContainer implements IEndportContainer {
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
        String dir = String.format("%s%sstore%sendports%sinfos", homeDir,File.separator,File.separator, File.separator);
        File fdir = new File(dir);
        if (!fdir.exists()) {
            fdir.mkdirs();
        }
        dir = String.format("%s%sdb_",dir,File.separator);
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
    public IEndport openport(IPrincipal principal, ListenMode mode) {
        if (endportInfoMap.containsKey(principal.key())) {
            byte[] b= endportInfoMap.get(principal.key());
            EndportInfo info = EndportInfo.load(b);
            if (info.listenMode != mode) {
                info.listenMode = mode;
                b=info.toBytes();
                endportInfoMap.put(info.principalKey, b);
                db.commit();
            }
            return  new DefaultEndport(info, homeDir, site);
        }
        return _createEndport(principal, mode);
    }

    @Override
    public IEndport openport(IPrincipal principal) throws CircuitException {
        byte[] b = this.endportInfoMap.get(principal.key());
        if (b == null) {
            throw new CircuitException("500","没有侦听网络");
        }
        EndportInfo info = EndportInfo.load(b);
        return  new DefaultEndport(info, homeDir, site);
    }

    @Override
    public IEndport openport(String principalKey) throws CircuitException {
        byte[] b = this.endportInfoMap.get(principalKey);
        if (b == null) {
            throw new CircuitException("500","没有侦听网络");
        }
        EndportInfo info = EndportInfo.load(b);
        return  new DefaultEndport(info, homeDir, site);
    }
    private IEndport _createEndport(IPrincipal principal, ListenMode mode) {
        EndportInfo info = new EndportInfo();
        info.principalKey = principal.key();
        info.listenMode = mode;
        endportInfoMap.put(info.principalKey, info.toBytes());
        db.commit();
        return new DefaultEndport(info, homeDir, site);
    }



}
