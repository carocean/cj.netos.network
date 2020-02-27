package cj.netos.network.peer;

import cj.netos.network.*;
import cj.netos.network.util.PropUtil;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.net.util.TcpFrameBox;
import cj.ultimate.util.StringUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class TcpConnection implements IConnection, IReconnection, INetworkServiceProvider {
    private final IOnopen onopen;
    private final IOnclose onclose;
    EventLoopGroup exepool;
    private Channel channel;
    private String protocol;
    private String host;
    private int port;
    private long heartbeat;
    private Map<String, String> props;
    private long reconnect_times;
    private long reconnect_interval;
    private int workThreadCount;
    private volatile boolean forbiddenReconnect;

    DefaultPipelineCombination pipelineCombination;
    private IOnreconnection onreconnection;

    public TcpConnection(IOnopen onopen, IOnclose onclose, IOnnotify onnotify) {
        pipelineCombination = new DefaultPipelineCombination(onnotify);
        this.onopen = onopen;
        this.onclose = onclose;
    }

    public TcpConnection() {
        this(null, null,null);
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public Object getService(String serviceId) {
        if ("$.prop.heartbeat".equals(serviceId)) {
            return heartbeat;
        }
        if ("$.prop.reconnect_times".equals(serviceId)) {
            return reconnect_times;
        }
        if ("$.prop.reconnect_interval".equals(serviceId)) {
            return reconnect_interval;
        }
        if ("$.connection".equals(serviceId)) {
            return this;
        }
        if ("$.channel".equals(serviceId)) {
            return channel;
        }
        if ("$.pipelineCombination".equals(serviceId)) {
            return pipelineCombination;
        }

        return null;
    }

    @Override
    public void forbiddenReconnect() {
        this.forbiddenReconnect = true;
    }

    @Override
    public void addLogicNetwork(ILogicNetwork lnetwork) {
        pipelineCombination.addLogicNetwork(lnetwork);
    }

    @Override
    public void removeLogicNetwork(String network) {
        pipelineCombination.removeLogicNetwork(network);
    }

    @Override
    public ILogicNetwork localNetwork(String networkName) {
        return pipelineCombination.localNetwork(networkName);
    }

    @Override
    public Set<String> enumLocalNetwork() {
        return pipelineCombination.enumLocalNetwork();
    }

    @Override
    public boolean isForbiddenReconnect() {
        return forbiddenReconnect;
    }

    @Override
    public void onclose() {
        if (onclose != null) {
            onclose.onclose();
        }
        if (exepool != null) {
            this.exepool.shutdownGracefully();
        }
        this.channel=null;
        if (props != null) {
            this.props.clear();
        }
        this.pipelineCombination=null;
        this.onreconnection=null;
    }

    @Override
    public void onopen() {
        if (onopen != null) {
            onopen.onopen();
        }
    }

    @Override
    public void reconnect() {
        if (this.forbiddenReconnect) {
            return;
        }
        //重连则把缓冲的本地网络清掉
        pipelineCombination.networkMap.clear();
        if (exepool != null) {
            if (!exepool.isShutdown() && !exepool.isTerminated()) {
                exepool.shutdownGracefully();
            }
            exepool = null;
        }
        Map<String, String> map = new HashMap<>();
        if (props != null) {
            map.putAll(props);
            props.clear();
        }
        connect(protocol, host, port, map);
        if (onreconnection != null) {
            onreconnection.onreconnect();
        }
    }

    @Override
    public void accept(IOnreconnection onreconnection) {
        this.onreconnection = onreconnection;
    }

    @Override
    public void connect(String protocol, String ip, int port, Map<String, String> props) {
        this.protocol = protocol;
        this.host = ip;
        this.port = port;
        this.props = props;
        parseProps(props);

        EventLoopGroup group = null;
        if (workThreadCount < 1) {
            group = new NioEventLoopGroup();
        } else {
            group = new NioEventLoopGroup(workThreadCount);
        }

        Bootstrap b = new Bootstrap();

        b.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
                .handler(new TcpClientGatewaySocketInitializer());
        try {
            this.channel = b.connect(ip, port).sync().channel();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private void parseProps(Map<String, String> props) {
        String strheartbeat = PropUtil.getValue(props
                .get("heartbeat"));
        if (StringUtil.isEmpty(strheartbeat)) {
            strheartbeat = "0";
        }
        this.heartbeat = Long.valueOf(strheartbeat);

        String strreconnect_times = PropUtil.getValue(props
                .get("reconnect_times"));
        if (StringUtil.isEmpty(strreconnect_times)) {
            strreconnect_times = "0";
        }
        this.reconnect_times = Long.valueOf(reconnect_times);

        String strreconnect_interval = PropUtil.getValue(props
                .get("reconnect_interval"));
        if (StringUtil.isEmpty(strreconnect_interval)) {
            strreconnect_interval = "5000";
        }
        this.reconnect_interval = Long.valueOf(strreconnect_interval);

        String workThreadCount = PropUtil.getValue(props
                .get("workThreadCount"));
        if (StringUtil.isEmpty(workThreadCount)) {
            workThreadCount = "0";
        }
        this.workThreadCount = Integer.valueOf(workThreadCount);

        CJSystem.logging().info(getClass(), String.format("连接属性：workThreadCount=%s,heartbeat=%s,reconnect_times=%s,reconnect_interval=%s",
                workThreadCount, heartbeat, reconnect_times, reconnect_interval));

    }

    @Override
    public void send(NetworkFrame frame) {
        PackFrame pack = new PackFrame((byte) 1, frame);
        byte[] box = TcpFrameBox.box(pack.toBytes());
        pack.dispose();
        ByteBuf bb = Unpooled.buffer();
        bb.writeBytes(box, 0, box.length);
        channel.writeAndFlush(bb);
    }

    @Override
    public boolean isConnected() {
        forbiddenReconnect();
        return channel.isWritable();
    }

    @Override
    public void close() {
        forbiddenReconnect();
        channel.close();
    }

    class TcpClientGatewaySocketInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new LengthFieldBasedFrameDecoder(81920, 0, 4, 0, 4));
            if (heartbeat > 0) {
                pipeline.addLast(new IdleStateHandler(0, heartbeat, 0, TimeUnit.SECONDS));
            }
            pipeline.addLast(new TcpClientHandler(TcpConnection.this));
        }

    }

}
