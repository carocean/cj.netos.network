package cj.netos.network.node;

import cj.netos.network.Castmode;

import java.util.Map;

public class NetworkInfo {
    String name;
    String title;
    Castmode frontendCastMode;
    Castmode backendCastmode;

    public void parse(Map<String, Object> item) {
        for (Map.Entry<String, Object> en : item.entrySet()) {
            name = en.getKey();
            _parseInfo(en.getValue());
            break;
        }
    }

    private void _parseInfo(Object value) {
        Map<String, Object> obj = (Map<String, Object>) value;
        System.out.println(obj);
        title = obj.get("title") + "";
        Map<String, Object> frontend = (Map<String, Object>) obj.get("frontend");
        for (Map.Entry<String, Object> en : frontend.entrySet()) {
            if ("castmode".equals(en.getKey())) {
                String castmode = (String) en.getValue();
                frontendCastMode = Castmode.valueOf(castmode);
            }
            break;
        }
        Map<String, Object> backend = (Map<String, Object>) obj.get("backend");
        for (Map.Entry<String, Object> en : backend.entrySet()) {
            if ("castmode".equals(en.getKey())) {
                String castmode = (String) en.getValue();
                backendCastmode = Castmode.valueOf(castmode);
            }
            break;
        }
    }

    public Castmode getBackendCastmode() {
        return backendCastmode;
    }

    public Castmode getFrontendCastMode() {
        return frontendCastMode;
    }

    public String getTitle() {
        return title;
    }

    public String getName() {
        return name;
    }
}
