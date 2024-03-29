package cj.netos.network.node;

import cj.netos.network.INetworkNodePlugin;
import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.node.server.TcpNetworkNodeServer;
import cj.netos.network.node.server.WSNetworkNodeServer;
import cj.studio.ecm.EcmException;
import cj.studio.ecm.annotation.CjService;

import java.io.FileNotFoundException;

@CjService(name = "networkNode", isExoteric = true)
public class NetworkNode implements INetworkNode {
    INetworkNodeServer nodeServer;//一个节点有且仅有一个服务器
    INetworkNodeConfig networkNodeConfig;//节点配置
    IEndpointerContainer endpointerContainer;//终节点容器
    IEndportContainer endportContainer;//终结口容器
    INetworkContainer networkContainer;//网络容器
    IPump pump;//抽水泵，也叫磁头，从终节点的接收队列和网络的终接点接收队列中抽取消息
    INetworkNodePlugin networkNodePlugin;
    INetworkServiceProvider site;

    @Override
    public void entrypoint(String home) throws FileNotFoundException {
        site = new NodeServiceProvider();

        networkNodeConfig = new NetworkNodeConfig();
        networkNodeConfig.load(home);


        nodeServer = createNetworkNodeServer(networkNodeConfig.getServerInfo());

        pump = new DefaultPump();

        networkContainer = new NetworkContainer();
        networkContainer.load(site, networkNodeConfig.getNetworkConfig(),home);

        INetworkNodePluginLoader loader = new NetworkNodePluginLoader();
        networkNodePlugin = loader.scanAssemblyAndLoad(networkNodeConfig, site);

        endportContainer = new EndportContainer(site);
        endpointerContainer = new EndpointContainer(site);


        pump.start(site);

        nodeServer.start();
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
            if ("$.network.endportContainer".equals(serviceId)) {
                return endportContainer;
            }
            if ("$.network.server".equals(serviceId)) {
                return nodeServer;
            }
            if ("$.network.plugin".equals(serviceId)) {
                return networkNodePlugin;
            }
            
            if ("$.network.config".equals(serviceId)) {
                return networkNodeConfig;
            }
            return null;
        }
    }
}
