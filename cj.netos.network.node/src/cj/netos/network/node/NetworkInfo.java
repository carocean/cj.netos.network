package cj.netos.network.node;

import cj.netos.network.BackendCastmode;
import cj.netos.network.FrontendCastmode;

import java.util.Map;

public class NetworkInfo {
    String name;
    String title;
    FrontendCastmode frontendCastMode;
    BackendCastmode backendCastmode;

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
                frontendCastMode = FrontendCastmode.valueOf(castmode);
            }
            break;
        }
        Map<String, Object> backend = (Map<String, Object>) obj.get("backend");
        for (Map.Entry<String, Object> en : backend.entrySet()) {
            if ("castmode".equals(en.getKey())) {
                String castmode = (String) en.getValue();
                backendCastmode = BackendCastmode.valueOf(castmode);
            }
            break;
        }
    }

    public BackendCastmode getBackendCastmode() {
        return backendCastmode;
    }

    public FrontendCastmode getFrontendCastMode() {
        return frontendCastMode;
    }

    public String getTitle() {
        return title;
    }

    public String getName() {
        return name;
    }
}
