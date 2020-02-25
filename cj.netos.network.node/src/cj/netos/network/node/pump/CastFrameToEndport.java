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

import java.util.List;

class CastFrameToEndport implements IReceiver {
    INetworkContainer networkContainer;
    IEndportContainer endportContainer;

    public CastFrameToEndport(INetworkContainer networkContainer, IEndportContainer endportContainer) {
        this.networkContainer = networkContainer;
        this.endportContainer = endportContainer;
    }

    @Override
    public void error(EventTask task, Throwable e, ILine line) {
        e.printStackTrace();
    }

    @Override
    public void receive(EventTask task, ILine line) throws CircuitException {
        IEndport endport = endportContainer.openport(task.getEndpointKey());
        IStreamSink sink = endport.openUpstream();
        NetworkFrame frame = sink.pullFirst();
        //不是任务的网络则退出，其它任务的网络可能会是它的目标，会拉它并移除
        String _networkName = frame.rootName();
        if (frame == null || !_networkName.equals(task.getNetwork())) {
            sink.close();
            return;
        }
        if (!networkContainer.containsNetwork(_networkName)) {
            sink.removeFirst();
            sink.close();
            return;
        }
        //只处理与task的网络一致的请求，不一定的将让给别的线程或下一次处理
        INetwork network = networkContainer.openNetwork(task.getNetwork());
        if (!network.hasMemberInBackend(task.getEndpointKey())) {
            _castToBackend(frame, task, network,line);
        }
        _castToFrontend(frame, task, network,line);
        sink.removeFirst().close();
    }

    private void _castToFrontend(NetworkFrame frame, EventTask task, INetwork network,ILine line) throws CircuitException {
        FrontendCastmode castmode = network.getFrontendCastmode();
        switch (castmode) {
            case selectcast:
                _frontend_selectcast(frame, network,line);
                break;
            case multicast:
                _frontend_multicast(frame, task, network,line);
                break;
            case unicast:
                _frontend_unicast(frame, task, network,line);
                break;
        }
    }

    private void _frontend_unicast(NetworkFrame frame, EventTask task, INetwork network,ILine line) throws CircuitException {
        List<String> members = network.listFrontendMembersExcept(task.getEndpointKey());
        _frontend_unicastExceptUpstream(members, frame, network,line);
    }

    private void _frontend_unicastExceptUpstream(List<String> members, NetworkFrame frame, INetwork network,ILine line) throws CircuitException {
        if (members.isEmpty()) {
            return;
        }
        int index = Math.abs(String.format("%s%s", System.currentTimeMillis(), frame.toString()).hashCode()) % members.size();
        String key = members.get(index);
        IEndport endport = endportContainer.openport(key);
        //对于只发送的侦听者拒发信息
        if (endport.getInfo().getListenMode() == ListenMode.upstream) {
            members.remove(key);
            _frontend_unicastExceptUpstream(members, frame, network,line);
            return;
        }
        endport.openDownstream().write(frame).close();
        EventTask downTask = new EventTask(Direction.downstream, key, network.getName());
        line.nextInput(downTask, this);
    }

    private void _frontend_selectcast(NetworkFrame frame, INetwork network,ILine line) throws CircuitException {
        String to_person = frame.head("to-person");
        String to_peer = frame.head("to-peer");
        String key = String.format("%s/%s", to_person, to_peer);
        if (!network.hasMemberInFrontend(key)) {
            CJSystem.logging().warn(getClass(), String.format("发送不目标：%s/%s不是本网络成员，侦丢弃。", to_person, to_peer));
            return;
        }
        IEndport endport = endportContainer.openport(key);
        endport.openDownstream().write(frame).close();
        EventTask downTask = new EventTask(Direction.downstream, key, network.getName());
        line.nextInput(downTask, this);
    }

    private void _frontend_multicast(NetworkFrame frame, EventTask task, INetwork network,ILine line) throws CircuitException {
        String[] members = network.listFrontendMembers();
        for (String key : members) {
            //不发自身
            if (key.equals(task.getEndpointKey())) {
                continue;
            }
            IEndport endport = endportContainer.openport(key);
            //对于只发送的侦听者拒发信息
            if (endport.getInfo().getListenMode() == ListenMode.upstream) {
                continue;
            }
            endport.openDownstream().write(frame).close();
            EventTask downTask = new EventTask(Direction.downstream, key, network.getName());
            line.nextInput(downTask, this);
        }
    }


    private void _castToBackend(NetworkFrame frame, EventTask task, INetwork network,ILine line) throws CircuitException {
        BackendCastmode castmode = network.getBackendCastmode();
        switch (castmode) {
            case selectcast:
                _backend_selectcast(frame, network,line);
                break;
            case multicast:
                _backend_multicast(frame, task, network,line);
                break;
            case unicast:
                _backend_unicast(frame, task, network,line);
                break;
        }
    }

    private void _backend_unicast(NetworkFrame frame, EventTask task, INetwork network,ILine line) throws CircuitException {
        List<String> members = network.listBackendMembersExcept(task.getEndpointKey());
        _backend_unicastExceptUpstream(members, frame, network,line);
    }

    private void _backend_unicastExceptUpstream(List<String> members, NetworkFrame frame, INetwork network,ILine line) throws CircuitException {
        if (members.isEmpty()) {
            return;
        }
        int index = Math.abs(String.format("%s%s", System.currentTimeMillis(), frame.toString()).hashCode()) % members.size();
        String key = members.get(index);
        IEndport endport = endportContainer.openport(key);
        //对于只发送的侦听者拒发信息
        if (endport.getInfo().getListenMode() == ListenMode.upstream) {
            members.remove(key);
            _backend_unicastExceptUpstream(members, frame, network,line);
            return;
        }
        endport.openDownstream().write(frame).close();
        EventTask downTask = new EventTask(Direction.downstream, key, network.getName());
        line.nextInput(downTask, this);
    }

    private void _backend_selectcast(NetworkFrame frame, INetwork network,ILine line) throws CircuitException {
        String to_person = frame.head("to-person");
        String to_peer = frame.head("to-peer");
        String key = String.format("%s/%s", to_person, to_peer);
        if (!network.hasMemberInFrontend(key)) {
            CJSystem.logging().warn(getClass(), String.format("发送不目标：%s/%s不是本网络成员，侦丢弃。", to_person, to_peer));
            return;
        }
        IEndport endport = endportContainer.openport(key);
        endport.openDownstream().write(frame).close();
        EventTask downTask = new EventTask(Direction.downstream, key, network.getName());
        line.nextInput(downTask, this);
    }

    private void _backend_multicast(NetworkFrame frame, EventTask task, INetwork network,ILine line) throws CircuitException {
        String[] members = network.listBackendMembers();
        for (String key : members) {
            //不发自身
            if (key.equals(task.getEndpointKey())) {
                continue;
            }
            IEndport endport = endportContainer.openport(key);
            //对于只发送的侦听者拒发信息
            if (endport.getInfo().getListenMode() == ListenMode.upstream) {
                continue;
            }
            endport.openDownstream().write(frame).close();
            EventTask downTask = new EventTask(Direction.downstream, key, network.getName());
            line.nextInput(downTask, this);
        }
    }
}