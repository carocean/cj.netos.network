package cj.netos.network.node;

import cj.netos.network.Castmode;
import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.IPrincipal;
import cj.netos.network.NetworkFrame;
import cj.studio.ecm.EcmException;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.gson2.com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkContainer implements INetworkContainer {
    NetworkConfig config;
    Map<String, INetwork> networks;
    private INetworkServiceProvider site;
    @Override
    public void load(INetworkServiceProvider site, NetworkConfig config) {
        this.config = config;
        this.site = site;
        this.networks = new HashMap<>();
        for (String key : config.getNetworks().keySet()) {
            NetworkInfo networkInfo = config.getNetworks().get(key);
            INetwork network = new DefaultNetwork(site, networkInfo);
            if (key.equals(config.getEventNetwork())) {
                if (network.getFrontendCastmode() == Castmode.selectcast) {
                    throw new EcmException("事件中心不支持selectcast分发模式");
                }
            }
            networks.put(key, network);
        }
    }

    @Override
    public boolean isAutoCreate() {
        return config.isAutoCreate();
    }

    @Override
    public String getEventNetwork() {
        return config.getEventNetwork();
    }

    @Override
    public INetwork getNetwork(String network) {
        return networks.get(network);
    }

    protected void notifyEventNetowrk(NetworkFrame frame) throws CircuitException {
        INetwork eventNetwork = getNetwork(getEventNetwork());
        eventNetwork.cast(frame);
    }

    @Override
    public void createNetwork(IPrincipal principal, String name, String title, Castmode frontendCastmode, Castmode backendCastmode) throws CircuitException {
        if (networks.containsKey(name)) {
            throw new EcmException("已存在网络:" + name);
        }
        INetwork nk = new DefaultNetwork(site, name, title, frontendCastmode, backendCastmode);
        networks.put(name, nk);

        NetworkFrame frame = new NetworkFrame("createNetwork /notify/ network/1.0");
        frame.parameter("person", principal.principal());
        frame.parameter("peer", principal.peer());
        frame.head("to-person", principal.principal());
        frame.head("to-peer", principal.peer());
        frame.head("status", "200");
        frame.head("ok");
        notifyEventNetowrk(frame);
    }

    @Override
    public void removeNetwork(IPrincipal principal, String network) throws CircuitException {
        networks.remove(network);

        NetworkFrame frame = new NetworkFrame("removeNetwork /notify/ network/1.0");
        frame.parameter("person", principal.principal());
        frame.parameter("peer", principal.peer());
        frame.head("to-person", principal.principal());
        frame.head("to-peer", principal.peer());
        frame.head("status", "200");
        frame.head("ok");
        notifyEventNetowrk(frame);
    }

    @Override
    public void viewNetwork(IPrincipal principal, String network) throws CircuitException {
        INetwork nw = networks.get(network);
        if (nw == null) {
            return;
        }
        ByteBuf bb = Unpooled.buffer();
        Map<String, Object> info = new HashMap<>();
        info.put("name", nw.getName());
        info.put("title", nw.getTitle());
        info.put("frontendCastmode", nw.getFrontendCastmode());
        info.put("backendCastmode", nw.getBackendCastmode());
        info.put("backendSinks", nw.enumBackendSinkKey());
        info.put("frontendSinks", nw.enumFrontendSinkKey());
        bb.writeBytes(new Gson().toJson(info).getBytes());
        NetworkFrame frame = new NetworkFrame("viewNetwork /notify/ network/1.0", bb);
        frame.parameter("person", principal.principal());
        frame.parameter("peer", principal.peer());
        frame.head("to-person", principal.principal());
        frame.head("to-peer", principal.peer());
        frame.head("status", "200");
        frame.head("ok");
        notifyEventNetowrk(frame);
    }

    @Override
    public void listNetwork(IPrincipal principal) throws CircuitException {
        ByteBuf bb = Unpooled.buffer();
        Map<String, Object> info = new HashMap<>();
        info.put("isAutoCreate", this.isAutoCreate());
        info.put("eventNetwork", this.getEventNetwork());
        List<Map<String, Object>> list = new ArrayList<>();
        for (String key : networks.keySet()) {
            INetwork nw = networks.get(key);
            Map<String, Object> obj = new HashMap<>();
            obj.put("name", nw.getName());
            obj.put("title", nw.getTitle());
            obj.put("frontendCastmode", nw.getFrontendCastmode());
            obj.put("backendCastmode", nw.getBackendCastmode());
            obj.put("frontendSinCount", nw.enumFrontendSinkKey().size());
            obj.put("backendSinkKey", nw.enumBackendSinkKey().size());
        }
        info.put("networks", list);
        bb.writeBytes(new Gson().toJson(info).getBytes());
        NetworkFrame frame = new NetworkFrame("listNetwork /notify/ network/1.0", bb);
        frame.parameter("person", principal.principal());
        frame.parameter("peer", principal.peer());
        frame.head("to-person", principal.principal());
        frame.head("to-peer", principal.peer());
        frame.head("status", "200");
        frame.head("ok");
        notifyEventNetowrk(frame);
    }

    @Override
    public void joinNetwork(IPrincipal principal, String network, boolean joinToFrontend) throws CircuitException {
        INetwork nw = networks.get(network);
        if (nw == null) {
            return;
        }
        nw.join(principal, joinToFrontend);

        ByteBuf bb = Unpooled.buffer();
        Map<String, Object> info = new HashMap<>();
        info.put("name", nw.getName());
        info.put("title", nw.getTitle());
        info.put("frontendCastmode", nw.getFrontendCastmode());
        info.put("backendCastmode", nw.getBackendCastmode());
        info.put("backendSinks", nw.enumBackendSinkKey());
        info.put("frontendSinks", nw.enumFrontendSinkKey());
        bb.writeBytes(new Gson().toJson(info).getBytes());
        NetworkFrame frame = new NetworkFrame("joinNetwork /notify/ network/1.0", bb);
        frame.parameter("person", principal.principal());
        frame.parameter("peer", principal.peer());
        frame.head("to-person", principal.principal());
        frame.head("to-peer", principal.peer());
        frame.head("status", "200");
        frame.head("ok");
        notifyEventNetowrk(frame);
    }

    @Override
    public void leaveNetwork(IPrincipal principal, String network, boolean isLeaveFrontend) throws CircuitException {
        INetwork nw = networks.get(network);
        if (nw == null) {
            return;
        }
        nw.leave(principal, isLeaveFrontend);

        ByteBuf bb = Unpooled.buffer();
        Map<String, Object> info = new HashMap<>();
        info.put("name", nw.getName());
        info.put("title", nw.getTitle());
        info.put("frontendCastmode", nw.getFrontendCastmode());
        info.put("backendCastmode", nw.getBackendCastmode());
        info.put("backendSinks", nw.enumBackendSinkKey());
        info.put("frontendSinks", nw.enumFrontendSinkKey());
        bb.writeBytes(new Gson().toJson(info).getBytes());
        NetworkFrame frame = new NetworkFrame("leaveNetwork /notify/ network/1.0", bb);
        frame.parameter("person", principal.principal());
        frame.parameter("peer", principal.peer());
        frame.head("to-person", principal.principal());
        frame.head("to-peer", principal.peer());
        frame.head("status", "200");
        frame.head("ok");
        notifyEventNetowrk(frame);
    }

    @Override
    public void onerror(IPrincipal principal, NetworkFrame frame, Throwable error) throws CircuitException {
        frame.parameter("person", principal.principal());
        frame.parameter("peer", principal.peer());
        frame.head("to-person", principal.principal());
        frame.head("to-peer", principal.peer());
        CircuitException ce = CircuitException.search(error);
        if (ce != null) {
            frame.head("status", ce.getStatus());
            frame.head(ce.getMessage().replace("\r", "").replace("\n", ""));
        } else {
            frame.head("status", "500");
            frame.head(error.getMessage().replace("\r", "").replace("\n", ""));
        }
        notifyEventNetowrk(frame);
    }

    @Override
    public void offline(IEndpointer endpointer) throws CircuitException {
        NetworkFrame frame = new NetworkFrame("offline /notify/ network/1.0");
        IPrincipal principal = endpointer.getPrincipal();
        frame.parameter("person", principal.principal());
        frame.parameter("peer", principal.peer());
        frame.head("to-person", principal.principal());
        frame.head("to-peer", principal.peer());
        frame.head("status", "200");
        frame.head("ok");
        notifyEventNetowrk(frame);
    }

    @Override
    public void online(IEndpointer endpointer) throws CircuitException {
        NetworkFrame frame = new NetworkFrame("online /notify/ network/1.0");
        IPrincipal principal = endpointer.getPrincipal();
        frame.parameter("person", principal.principal());
        frame.parameter("peer", principal.peer());
        frame.head("to-person", principal.principal());
        frame.head("to-peer", principal.peer());
        frame.head("status", "200");
        frame.head("ok");
        notifyEventNetowrk(frame);
    }

    @Override
    public void onnofity(NetworkFrame frame, IPrincipal principal) throws CircuitException {
        frame.head("to-person", principal.principal());
        frame.head("to-peer", principal.peer());
        frame.head("status", "200");
        frame.head("ok");
        notifyEventNetowrk(frame);
    }
}
