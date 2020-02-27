package cj.netos.network.node;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.IPrincipal;
import cj.netos.network.ListenMode;
import cj.studio.ecm.net.CircuitException;
import org.apache.jdbm.DB;
import org.apache.jdbm.DBMaker;

import java.io.File;
import java.util.*;

public class EndportContainer implements IEndportContainer, IEndportInfoChanged {
    final static String KEY_MAP = "endports";
    private final INetworkServiceProvider site;
    String homeDir;
    Map<String, byte[]> endportInfoMap;
    DB db;
    Map<String, List<String>> _key_person_index;//key是person以索引peers
    Map<String, List<String>> _key_peer_index;//key是peer以索引persons

    public EndportContainer(INetworkServiceProvider site) {
        INetworkNodeConfig config = (INetworkNodeConfig) site.getService("$.network.config");
        homeDir = config.home();
        this.site = site;
        _key_peer_index = new HashMap<>();
        _key_person_index = new HashMap<>();
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
        //建立索引
        for (String key : map.keySet()) {
            _addIndex(key);
        }
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
            EndportInfo info = EndportInfo.load(b, this);
            return new DefaultEndport(info, homeDir, site);
        }
        return _createEndport(principalKey);
    }

    private IEndport _createEndport(String principalKey) {
        EndportInfo info = new EndportInfo(this);
        info.principalKey = principalKey;
        endportInfoMap.put(info.principalKey, info.toBytes());
        db.commit();
        _addIndex(principalKey);
        return new DefaultEndport(info, homeDir, site);
    }

    @Override
    public void addListenNetwork(EndportInfo info) {
        endportInfoMap.put(info.principalKey, info.toBytes());
        db.commit();
        _addIndex(info.principalKey);
    }

    @Override
    public void removeNetworkListenmode(EndportInfo info) {
        endportInfoMap.remove(info.principalKey);
        db.commit();
        _removeIndex(info.principalKey);
    }


    private void _addIndex(String key) {
        int pos = key.indexOf("/");
        if (pos < 1) {
            return;
        }
        String person = key.substring(0, pos);
        String peer = key.substring(pos + 1);
        List<String> peers = _key_person_index.get(person);
        if (peers == null) {
            peers = new ArrayList<>();
            _key_person_index.put(person, peers);
        }
        if (!peers.contains(peer)) {
            peers.add(peer);
        }
        List<String> persons = _key_peer_index.get(peer);
        if (persons == null) {
            persons = new ArrayList<>();
            _key_peer_index.put(peer, persons);
        }
        if (!persons.contains(person)) {
            persons.add(person);
        }
    }

    private void _removeIndex(String key) {
        int pos = key.indexOf("/");
        if (pos < 1) {
            return;
        }
        String person = key.substring(0, pos);
        String peer = key.substring(pos + 1);
        List<String> peers = _key_person_index.get(person);
        if (peers != null) {
            peers.remove(peer);
        }
        List<String> persons = _key_peer_index.get(peer);
        if (persons != null) {
            persons.remove(person);
        }
    }

    @Override
    public List<String> findPersonByPeer(String peer) {
        List<String> persons = _key_peer_index.get(peer);
        if (persons == null) {
            persons = new ArrayList<>();
            _key_peer_index.put(peer, persons);
        }
        return persons;
    }

    @Override
    public List<String> findPeersByPerson(String person) {
        List<String> peers = _key_person_index.get(person);
        if (peers == null) {
            peers = new ArrayList<>();
            _key_person_index.put(person, peers);
        }
        return peers;
    }
}
