package cj.netos.network.node;

import cj.netos.network.BackendCastmode;
import cj.netos.network.FrontendCastmode;
import cj.netos.network.IPrincipal;
import org.apache.jdbm.DB;
import org.apache.jdbm.DBMaker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DefaultNetwork implements INetwork {

    private NetworkInfo info;
    private Map<String, byte[]> frontendMembers;
    private Map<String, byte[]> backendMembers;
    DB db;

    public DefaultNetwork(NetworkInfo info, String home) {
        this.info = info;
        String path = String.format("%s%sstore%snetworks%s%s", home, File.separator, File.separator, File.separator, info.name);
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        path = String.format("%s%sdb_", path, File.separator);
        db = DBMaker.openFile(path).disableLocking().closeOnExit().make();
        frontendMembers = db.getHashMap("frontend");
        if (frontendMembers == null) {
            frontendMembers = db.createHashMap("frontend");
            db.commit();
        }
        backendMembers = db.getHashMap("backend");
        if (backendMembers == null) {
            backendMembers = db.createHashMap("backend");
            db.commit();
        }
    }

    @Override
    public void close() {
        db.close();
        info=null;
    }


    @Override
    public void addMember(IPrincipal principal, boolean joinToFrontend) {
        if (joinToFrontend) {
            frontendMembers.put(principal.key(), principal.toBytes());
        } else {
            backendMembers.put(principal.key(), principal.toBytes());
        }
        db.commit();
    }

    @Override
    public void removeMember(IPrincipal principal) {
        frontendMembers.remove(principal.key());
        backendMembers.remove(principal.key());
        db.commit();
    }

    @Override
    public boolean hasMemberInFrontend(String key) {
        return frontendMembers.containsKey(key);
    }

    @Override
    public boolean hasMemberInBackend(String key) {
        return backendMembers.containsKey(key);
    }

    @Override
    public String getName() {
        return info.getName();
    }

    @Override
    public String getTitle() {
        return info.getTitle();
    }

    @Override
    public FrontendCastmode getFrontendCastmode() {
        return info.getFrontendCastMode();
    }

    @Override
    public BackendCastmode getBackendCastmode() {
        return info.getBackendCastmode();
    }

    @Override
    public int frontendMemberCount() {
        return frontendMembers.size();
    }

    @Override
    public int backendMemberCount() {
        return backendMembers.size();
    }

    @Override
    public String[] listFrontendMembers() {
        return this.frontendMembers.keySet().toArray(new String[0]);
    }

    @Override
    public String[] listBackendMembers() {
        return this.backendMembers.keySet().toArray(new String[0]);
    }

    @Override
    public List<String> listFrontendMembersExcept(String key) {
        if (frontendMembers.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> keys = new ArrayList<>();
        for (String inkey : frontendMembers.keySet()) {
            if (inkey.equals(key)) {
                continue;
            }
            keys.add(inkey);
        }
        return keys;
    }

    @Override
    public List<String> listBackendMembersExcept(String key) {
        if (backendMembers.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> keys = new ArrayList<>();
        for (String inkey : backendMembers.keySet()) {
            if (inkey.equals(key)) {
                continue;
            }
            keys.add(inkey);
        }
        return keys;
    }
}
