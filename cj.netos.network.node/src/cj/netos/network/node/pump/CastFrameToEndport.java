package cj.netos.network.node.pump;

import cj.netos.network.BackendCastmode;
import cj.netos.network.FrontendCastmode;
import cj.netos.network.ListenMode;
import cj.netos.network.NetworkFrame;
import cj.netos.network.node.*;
import cj.netos.network.node.eventloop.ILine;
import cj.netos.network.node.eventloop.IReceiver;
import cj.netos.network.node.eventloop.EventTask;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.gson2.com.google.gson.Gson;
import cj.ultimate.util.StringUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class CastFrameToEndport implements IReceiver {
    INetworkContainer networkContainer;
    IEndportContainer endportContainer;

    public CastFrameToEndport(INetworkContainer networkContainer, IEndportContainer endportContainer) {
        this.networkContainer = networkContainer;
        this.endportContainer = endportContainer;
    }

    @Override
    public void error(EventTask task, Throwable error, ILine line) {
        IEndpointerContainer endpointerContainer = (IEndpointerContainer) line.site().getService("$.network.endpointerContainer");
        IEndpointer endpointer = endpointerContainer.endpointer(task.getEndpointKey());
        ByteBuf bb = Unpooled.buffer();
        Map<String, Object> map = new HashMap<>();
        StringWriter buffer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(buffer);
        error.printStackTrace(printWriter);
        map.put("cause", buffer.toString());
        bb.writeBytes(new Gson().toJson(map).getBytes());

        NetworkFrame back = new NetworkFrame(String.format("error /%s network/1.0", task.getNetwork()), bb);
        String key = task.getEndpointKey();
        int pos = key.indexOf("/");
        String person = key.substring(0, pos);
        String peer = key.substring(pos + 1);
        back.head("sender-person", person);
        back.head("sender-peer", peer);
        CircuitException ce = CircuitException.search(error);
        if (ce != null) {
            back.head("status", ce.getStatus());
            back.head("message", ce.getMessage() + "");
        } else {
            back.head("status", "500");
            back.head("message", error.getMessage() + "");
        }
        endpointer.write(back);
    }

    @Override
    public void receive(EventTask task, ILine line) throws CircuitException {
        IEndport endport = endportContainer.openport(task.getEndpointKey());
        IStreamSink sink = endport.openUpstream(task.getNetwork());
        while (true) {
            NetworkFrame frame = sink.pullFirst();
            //不是任务的网络则退出，其它任务的网络可能会是它的目标，会拉它并移除
            if (frame == null) {
                sink.close();
                return;
            }
            String networkName = frame.rootName();
            if (!networkContainer.containsNetwork(networkName)) {
                sink.removeFirst();
                sink.close();
                return;
            }
            //只处理与task的网络一致的请求，不一定的将让给别的线程或下一次处理
            INetwork network = networkContainer.openNetwork(networkName);
            if (!network.hasMemberInBackend(task.getEndpointKey())) {
                _castToBackend(frame, task, network, line);
            }
            if (network.getFrontendCastmode() != FrontendCastmode.none) {
                _castToFrontend(frame, task, network, line);
            }
            sink.removeFirst().close();
        }
    }

    private void _castToFrontend(NetworkFrame frame, EventTask task, INetwork network, ILine line) throws CircuitException {
        FrontendCastmode castmode = network.getFrontendCastmode();
        switch (castmode) {
            case selectcast:
                _frontend_selectcast(frame, network, line);
                break;
            case multicast:
                _frontend_multicast(frame, task, network, line);
                break;
            case unicast:
                _frontend_unicast(frame, task, network, line);
                break;
        }
    }

    private void _frontend_unicast(NetworkFrame frame, EventTask task, INetwork network, ILine line) throws CircuitException {
        List<String> members = network.listFrontendMembersExcept(task.getEndpointKey());
        _frontend_unicastExceptUpstream(members, frame, network, line);
    }

    private void _frontend_unicastExceptUpstream(List<String> members, NetworkFrame frame, INetwork network, ILine line) throws CircuitException {
        if (members.isEmpty()) {
            return;
        }
        int index = Math.abs(String.format("%s%s", System.currentTimeMillis(), frame.toString()).hashCode()) % members.size();
        String key = members.get(index);
        IEndport endport = endportContainer.openport(key);
        //对于只发送的侦听者拒发信息
        if (endport.isListenMode(network.getName(), ListenMode.upstream)) {
            members.remove(key);
            _frontend_unicastExceptUpstream(members, frame, network, line);
            return;
        }
        endport.openDownstream(network.getName()).write(frame).close();
        EventTask downTask = new EventTask(Direction.downstream, key, network.getName());
        line.nextInput(downTask, this);
    }

    private void _frontend_selectcast(NetworkFrame frame, INetwork network, ILine line) throws CircuitException {
        String to_person = frame.head("to-person");
        String to_peer = frame.head("to-peer");
        boolean is_toperson_empty = StringUtil.isEmpty(to_person);
        boolean is_topeer_empty = StringUtil.isEmpty(to_peer);
        if (is_toperson_empty && is_topeer_empty) {
            CJSystem.logging().warn(getClass(), "发送目标不确定：to_person和to_peer请求头均为空，侦丢弃。" + frame);
            return;
        }
        if (is_toperson_empty) {
            //按peer查找person列表并都发，这很危险，如果用户之间有peer相同则信息等同于泄漏
            List<String> persons = endportContainer.findPersonByPeer(to_peer);
            if (persons == null || persons.isEmpty()) {
                CJSystem.logging().warn(getClass(), String.format("该peer:%s没有关联的person，侦丢弃。%s", to_peer, frame));
                return;
            }
            for (String person : persons) {
                _frontend_selectcast_whole(frame, person, to_peer, network, line);
            }
            return;
        }
        if (is_topeer_empty) {
            //按person查找peer列表并都发
            List<String> peers = endportContainer.findPeersByPerson(to_person);
            if (peers == null || peers.isEmpty()) {
                CJSystem.logging().warn(getClass(), String.format("该person:%s没有关联的peer，侦丢弃。%s", to_person, frame));
                return;
            }
            for (String peer : peers) {
                _frontend_selectcast_whole(frame, to_person, peer, network, line);
            }
            return;
        }
        _frontend_selectcast_whole(frame, to_person, to_peer, network, line);
    }

    private void _frontend_selectcast_whole(NetworkFrame frame, String to_person, String to_peer, INetwork network, ILine line) throws CircuitException {
        String key = String.format("%s/%s", to_person, to_peer);
        if (!network.hasMemberInFrontend(key)) {
            CJSystem.logging().warn(getClass(), String.format("发送目标：%s/%s不是本网络成员，侦丢弃。%s", to_person, to_peer, frame));
            return;
        }
        IEndport endport = endportContainer.openport(key);
        endport.openDownstream(network.getName()).write(frame).close();
        EventTask downTask = new EventTask(Direction.downstream, key, network.getName());
        line.nextInput(downTask, this);
    }

    private void _frontend_multicast(NetworkFrame frame, EventTask task, INetwork network, ILine line) throws CircuitException {
        String[] members = network.listFrontendMembers();
        for (String key : members) {
            //不发自身
            if (key.equals(task.getEndpointKey())) {
                continue;
            }
            IEndport endport = endportContainer.openport(key);
            //对于只发送的侦听者拒发信息
            if (endport.isListenMode(network.getName(), ListenMode.upstream)) {
                continue;
            }
            endport.openDownstream(network.getName()).write(frame).close();
            EventTask downTask = new EventTask(Direction.downstream, key, network.getName());
            line.nextInput(downTask, this);
        }
    }


    private void _castToBackend(NetworkFrame frame, EventTask task, INetwork network, ILine line) throws CircuitException {
        BackendCastmode castmode = network.getBackendCastmode();
        switch (castmode) {
            case selectcast:
                _backend_selectcast(frame, network, line);
                break;
            case multicast:
                _backend_multicast(frame, task, network, line);
                break;
            case unicast:
                _backend_unicast(frame, task, network, line);
                break;
        }
    }

    private void _backend_unicast(NetworkFrame frame, EventTask task, INetwork network, ILine line) throws CircuitException {
        List<String> members = network.listBackendMembersExcept(task.getEndpointKey());
        _backend_unicastExceptUpstream(members, frame, network, line);
    }

    private void _backend_unicastExceptUpstream(List<String> members, NetworkFrame frame, INetwork network, ILine line) throws CircuitException {
        if (members.isEmpty()) {
            return;
        }
        int index = Math.abs(String.format("%s%s", System.currentTimeMillis(), frame.toString()).hashCode()) % members.size();
        String key = members.get(index);
        IEndport endport = endportContainer.openport(key);
        //对于只发送的侦听者拒发信息
        if (endport.isListenMode(network.getName(), ListenMode.upstream)) {
            members.remove(key);
            _backend_unicastExceptUpstream(members, frame, network, line);
            return;
        }
        endport.openDownstream(network.getName()).write(frame).close();
        EventTask downTask = new EventTask(Direction.downstream, key, network.getName());
        line.nextInput(downTask, this);
    }

    private void _backend_selectcast(NetworkFrame frame, INetwork network, ILine line) throws CircuitException {
        String to_person = frame.head("to-person");
        String to_peer = frame.head("to-peer");
        boolean is_toperson_empty = StringUtil.isEmpty(to_person);
        boolean is_topeer_empty = StringUtil.isEmpty(to_peer);
        if (is_toperson_empty && is_topeer_empty) {
            CJSystem.logging().warn(getClass(), "发送目标不确定：to_person和to_peer请求头均为空，侦丢弃。" + frame);
            return;
        }
        if (is_toperson_empty) {
            //按peer查找person列表并都发，这很危险，如果用户之间有peer相同则信息等同于泄漏
            List<String> persons = endportContainer.findPersonByPeer(to_peer);
            if (persons == null || persons.isEmpty()) {
                CJSystem.logging().warn(getClass(), String.format("该peer:%s没有关联的person，侦丢弃。%s", to_peer, frame));
                return;
            }
            for (String person : persons) {
                _backend_selectcast_whole(frame, person, to_peer, network, line);
            }
            return;
        }
        if (is_topeer_empty) {
            //按person查找peer列表并都发
            List<String> peers = endportContainer.findPeersByPerson(to_person);
            if (peers == null || peers.isEmpty()) {
                CJSystem.logging().warn(getClass(), String.format("该person:%s没有关联的peer，侦丢弃。%s", to_person, frame));
                return;
            }
            for (String peer : peers) {
                _backend_selectcast_whole(frame, to_person, peer, network, line);
            }
            return;
        }
        _backend_selectcast_whole(frame, to_person, to_peer, network, line);
    }

    private void _backend_selectcast_whole(NetworkFrame frame, String to_person, String to_peer, INetwork network, ILine line) throws CircuitException {
        String key = String.format("%s/%s", to_person, to_peer);
        if (!network.hasMemberInFrontend(key)) {
            CJSystem.logging().warn(getClass(), String.format("发送目标：%s/%s不是本网络成员，侦丢弃。%s", to_person, to_peer, frame));
            return;
        }
        IEndport endport = endportContainer.openport(key);
        endport.openDownstream(network.getName()).write(frame).close();
        EventTask downTask = new EventTask(Direction.downstream, key, network.getName());
        line.nextInput(downTask, this);
    }

    private void _backend_multicast(NetworkFrame frame, EventTask task, INetwork network, ILine line) throws CircuitException {
        String[] members = network.listBackendMembers();
        for (String key : members) {
            //不发自身
            if (key.equals(task.getEndpointKey())) {
                continue;
            }
            IEndport endport = endportContainer.openport(key);
            //对于只发送的侦听者拒发信息
            if (endport.isListenMode(network.getName(), ListenMode.upstream)) {
                continue;
            }
            endport.openDownstream(network.getName()).write(frame).close();
            EventTask downTask = new EventTask(Direction.downstream, key, network.getName());
            line.nextInput(downTask, this);
        }
    }
}