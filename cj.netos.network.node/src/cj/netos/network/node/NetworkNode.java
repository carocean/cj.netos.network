package cj.netos.network.node;

import cj.netos.network.INetworkNodePlugin;
import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.node.server.TcpNetworkNodeServer;
import cj.netos.network.node.server.WSNetworkNodeServer;
import cj.studio.ecm.EcmException;
import cj.studio.ecm.annotation.CjService;
import org.apache.jdbm.DB;
import org.apache.jdbm.DBMaker;

import java.io.File;
import java.io.FileNotFoundException;

@CjService(name = "networkNode", isExoteric = true)
public class NetworkNode implements INetworkNode {
    INetworkNodeServer nodeServer;//一个节点有且仅有一个服务器
    INetworkNodeConfig networkNodeConfig;//节点配置
    IEndpointerContainer endpointerContainer;//终节点容器
    INetworkContainer networkContainer;//网络容器
    IPump pump;//抽水泵，也叫磁头，从终节点的接收队列和网络的终接点接收队列中抽取消息
    INetworkNodePlugin networkNodePlugin;
    INetworkServiceProvider site;
    DB db;

    @Override
    public void entrypoint(String home) throws FileNotFoundException {
        site = new NodeServiceProvider();

        networkNodeConfig = new NetworkNodeConfig();
        networkNodeConfig.load(home);

        db = initDb(home);

        nodeServer = createNetworkNodeServer(networkNodeConfig.getServerInfo());

        pump = new DefaultPump();

        networkContainer = new NetworkContainer();
        networkContainer.load(site,networkNodeConfig.getNetworkConfig());

        INetworkNodePluginLoader loader = new NetworkNodePluginLoader();
        networkNodePlugin = loader.scanAssemblyAndLoad(networkNodeConfig, site);

        endpointerContainer = new EndpointContainer(site);


        pump.start(site);

        nodeServer.start();
    }

    private DB initDb(String home) {
        String storeDir = String.format("%s%scache", home, File.separator);
        File f = new File(storeDir);
        if (!f.exists()) {
            f.mkdirs();
        }
        storeDir=String.format("%s%sdb_",storeDir,File.separator);
        return DBMaker.openFile(storeDir).closeOnExit().disableCache().disableLocking().disableTransactions().make();
    }


    protected INetworkNodeServer createNetworkNodeServer(ServerInfo serverInfo) {
        switch (serverInfo.getProtocol()) {
            case "tcp":
                return new TcpNetworkNodeServer(site);
            case "ws":
            case "wss":
                return new WSNetworkNodeServer(site);
//            case "http":
//            case "https":
//                return new HttpNetworkNodeServer(site);
            default:
                throw new EcmException(String.format("不支持的协议：%s", serverInfo.getProtocol()));
        }
    }

    class NodeServiceProvider implements INetworkServiceProvider {

        @Override
        public Object getService(String serviceId) {
            if ("$.network.pump".equals(serviceId)) {
                return pump;
            }
            if ("$.network.networkContainer".equals(serviceId)) {
                return networkContainer;
            }
            if ("$.network.endpointerContainer".equals(serviceId)) {
                return endpointerContainer;
            }
            if ("$.network.server".equals(serviceId)) {
                return nodeServer;
            }
            if ("$.network.plugin".equals(serviceId)) {
                return networkNodePlugin;
            }
            if ("$.network.db".equals(serviceId)) {
                return db;
            }
            if ("$.network.config".equals(serviceId)) {
                return networkNodeConfig;
            }
            return null;
        }
    }
}
