package cj.netos.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultPrincipal implements IPrincipal {
    String principal;
    String peer;
    List<String> roles;
    Map<String, Object> properties;

    public DefaultPrincipal(String principal, String peer, List<String> roles) {
        this.principal = principal;
        this.peer = peer;
        this.properties = new HashMap<>();
        this.roles = roles;
        if (this.roles == null) {
            this.roles = new ArrayList<>();
        }
    }

    @Override
    public String principal() {
        return principal;
    }

    @Override
    public String peer() {
        return peer;
    }

    @Override
    public boolean roleIn(String role) {
        return false;
    }

    @Override
    public Object property(String key) {
        return properties.get(key);
    }

    @Override
    public void property(String key, Object value) {
        properties.put(key, value);
    }

    @Override
    public boolean roleStart(String s) {
        for (String r : roles) {
            if (r.startsWith(s)) {
                return true;
            }
        }
        return false;
    }
}
