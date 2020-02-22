package cj.netos.network.node;

import cj.netos.network.INetworkNodePlugin;
import cj.netos.network.INetworkServiceProvider;
import cj.studio.ecm.Assembly;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.EcmException;
import cj.studio.ecm.IAssembly;
import cj.studio.ecm.net.CircuitException;

import java.io.File;
import java.io.FilenameFilter;

class NetworkNodePluginLoader implements INetworkNodePluginLoader {
    IAssembly assembly;
    @Override
    public INetworkNodePlugin scanAssemblyAndLoad(INetworkNodeConfig config, INetworkServiceProvider parent) {
        String home = config.home();
        String appDir = String.format("%s%splugin", home, File.separator);
        File dir = new File(appDir);
        if (!dir.exists()) {
            throw new EcmException("程序集目录不存在:" + dir);
        }
        File[] assemblies = dir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });
        if (assemblies.length == 0) {
            return null;
        }
        if (assemblies.length > 1) {
            throw new EcmException("定义了多个程序集:" + home);
        }
        String fn = assemblies[0].getAbsolutePath();
        IAssembly app = Assembly.loadAssembly(fn);
        app.start();
        this.assembly = app;
        INetworkNodePlugin plugin = (INetworkNodePlugin) app.workbin().part("$.cj.netos.network.node.plugin");
        if (plugin == null) {
            throw new EcmException("程序集验证失败，原因：INetworkNodePlugin 的派生实现,请检查入口服务名：$.cj.netos.network.node.plugin");
        }
        INetworkServiceProvider site = new NodeAppServiceSite(parent);
        try {
            plugin.onstart(appDir, site);
        } catch (CircuitException e) {
            throw new EcmException(e);
        }
        CJSystem.logging().info(NetworkNodePluginLoader.class, String.format("节点应用已启动。"));
        return plugin;
    }
    @Override
    public void stopAssembly() {
        if (assembly != null) {
            assembly.stop();
            assembly = null;
        }
    }

    private static class NodeAppServiceSite implements INetworkServiceProvider {

        private final INetworkServiceProvider parent;

        public NodeAppServiceSite(INetworkServiceProvider parent) {
            this.parent = parent;
        }

        @Override
        public Object getService(String name) {
            return parent.getService(name);
        }
    }
}
