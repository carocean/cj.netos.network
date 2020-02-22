package cj.netos.network.plugin;

import cj.netos.network.*;
import cj.studio.ecm.EcmException;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.net.CircuitException;
import okhttp3.OkHttpClient;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@CjService(name = "$.cj.netos.network.node.plugin", isExoteric = true)
public class NetworkNodePlugin implements INetworkNodePlugin, INetworkServiceProvider {
    OkHttpClient client;
    PluginConfig config;

    @Override
    public void onstart(String pluginHome, INetworkServiceProvider site) throws CircuitException {
        String confFile = String.format("%s%sconf%splugin.yaml", pluginHome, File.separator, File.separator);
        Reader reader = null;
        try {
            reader = new FileReader(confFile);
        } catch (FileNotFoundException e) {
            throw new CircuitException("500", e);
        }
        Yaml nodeyaml = new Yaml();
        Map<String, Object> node = nodeyaml.load(reader);
        config = new PluginConfig();
        config.parse(node);
        client = new OkHttpClient().newBuilder()
                .readTimeout(config.readTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(config.writeTimeout(), TimeUnit.MILLISECONDS)
                .connectTimeout(config.connectTimeout(), TimeUnit.MILLISECONDS)
                .build();
    }

    @Override
    public Object getService(String name) {
        if ("$.config".equals(name)) {
            return config;
        }
        if ("$.http".equals(name)) {
            return client;
        }
        return null;
    }

    @Override
    public ICheckRights createCheckRights() {
        return new ICheckRights() {
            @Override
            public boolean checkRights(String instruction, IPrincipal principal) {
                boolean hasRights = true;
                switch (instruction) {
                    case "createNetwork":
                    case "removeNetwork":
                        hasRights = principal.roleStart("app:administrators")
                                || principal.roleStart("tenant:administrators")
                                || principal.roleStart("platform:administrators");
                        break;
                }
                return hasRights;
            }
        };
    }

    @Override
    public void combine(IPipeline pipeline) {
        pipeline.append(new AuthValve(this));
    }

    @Override
    public void demolish(IPipeline pipeline) {

    }
}
