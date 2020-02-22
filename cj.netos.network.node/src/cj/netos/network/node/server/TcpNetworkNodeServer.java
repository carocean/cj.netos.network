package cj.netos.network.node.server;

import cj.netos.network.node.INetworkNodeConfig;
import cj.netos.network.node.INetworkNodeServer;
import cj.netos.network.IPipelineCombination;
import cj.netos.network.node.ServerInfo;
import cj.netos.network.node.initializer.TcpChannelInitializer;
import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.node.pipeline.DefaultPipelineCombination;
import cj.netos.network.util.PropUtil;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.EcmException;
import cj.ultimate.util.StringUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.internal.SystemPropertyUtil;

import java.util.Map;

public class TcpNetworkNodeServer implements INetworkNodeServer, INetworkServiceProvider {
    INetworkServiceProvider site;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    boolean isStarted;
    private int bossThreadCount;
    private int workThreadCount;
    private ServerInfo serverInfo;
    private long heartbeat;
    private long overtimes;
    private IPipelineCombination combination;

    public TcpNetworkNodeServer(INetworkServiceProvider site) {
        this.site = site;
    }


    @Override
    public Object getService(String serviceId) {
        if ("$.server.info".equals(serviceId)) {
            return serverInfo;
        }
        if ("$.server.pipeline.combination".equals(serviceId)) {
            return combination;
        }
        if ("$.server.heartbeat".equals(serviceId)) {
            return heartbeat;
        }
        if ("$.server.overtimes".equals(serviceId)) {
            return overtimes;
        }

        return site.getService(serviceId);
    }

    @Override
    public void stop() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        isStarted = false;
        site = null;
    }

    @Override
    public void start() {
        combination = new DefaultPipelineCombination();
        INetworkNodeConfig config = (INetworkNodeConfig) site.getService("$.network.config");
        this.serverInfo = config.getServerInfo();
        if (isStarted) {
            throw new EcmException(String.format("服务器:%s已启动", serverInfo));
        }
        parseProps(serverInfo.getProps());

        bossGroup = new NioEventLoopGroup(bossThreadCount);
        workerGroup = new NioEventLoopGroup(workThreadCount);
        ServerBootstrap b = new ServerBootstrap();
        try {
            b.group(bossGroup, workerGroup).childOption(ChannelOption.SO_KEEPALIVE, true)
                    .channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new TcpChannelInitializer(this));

            // Bind and start to accept incoming connections.
            Channel ch = null;
            if ("localhost".equals(serverInfo.getHost())) {
                ch = b.bind(serverInfo.getPort()).sync().channel();
            } else {
                ch = b.bind(serverInfo.getHost(), serverInfo.getPort()).sync().channel();
            }
            ch.closeFuture();// .sync();
            isStarted = true;
            CJSystem.logging().info(getClass(), String.format("服务已启动，地址:%s", serverInfo.toString()));
        } catch (InterruptedException e) {
            throw new EcmException(e);
        }
    }

    private void parseProps(Map<String, Object> props) {
        this.bossThreadCount = 1;
        String strbossThreadCount = PropUtil.getValue(props.get("bossThreadCount"));
        if (!StringUtil.isEmpty(strbossThreadCount)) {
            this.bossThreadCount = Integer.valueOf(strbossThreadCount);
        }
        this.workThreadCount = 0;
        String strworkThreadCount = PropUtil.getValue(props.get("workThreadCount"));
        if (!StringUtil.isEmpty(strworkThreadCount)) {
            this.workThreadCount = Integer.valueOf(strworkThreadCount);
        } else {
            this.workThreadCount = Math.max(1, SystemPropertyUtil.getInt(
                    "io.netty.eventLoopThreads", Runtime.getRuntime().availableProcessors() * 2));
        }
        this.heartbeat = 0;
        String ht = PropUtil.getValue(props.get("heartbeat"));
        if (!StringUtil.isEmpty(ht)) {
            this.heartbeat = Long.valueOf(ht);
        }
        if (this.heartbeat > 0) {
            CJSystem.logging().info(getClass(), String.format("开启了心跳，策略：heartbeat=%s,overtimes=%s", heartbeat, overtimes));
        }
        this.overtimes = 0;
        String ot = PropUtil.getValue(props.get("overtimes"));
        if (!StringUtil.isEmpty(ot)) {
            this.overtimes = Long.valueOf(ot);
        }

    }


}
