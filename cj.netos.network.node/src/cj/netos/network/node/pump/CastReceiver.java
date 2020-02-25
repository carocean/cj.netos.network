package cj.netos.network.node.pump;

import cj.netos.network.BackendCastmode;
import cj.netos.network.FrontendCastmode;
import cj.netos.network.NetworkFrame;
import cj.netos.network.node.*;
import cj.netos.network.node.eventloop.IReceiver;
import cj.netos.network.node.eventloop.Task;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.net.CircuitException;

class CastReceiver implements IReceiver {
    INetworkContainer networkContainer;
    IEndportContainer endportContainer;

    public CastReceiver(INetworkContainer networkContainer, IEndportContainer endportContainer) {
        this.networkContainer = networkContainer;
        this.endportContainer = endportContainer;
    }

    @Override
    public void error(Task task, Throwable e) {
        e.printStackTrace();
    }

    @Override
    public void receive(Task task) throws CircuitException {
        IEndport endport = endportContainer.openport(task.getEndpoint());
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
        if (!network.hasMemberInBackend(task.getEndpoint())) {
            _castToBackend(frame, network);
        }
        _castToFrontend(frame, network);
        sink.removeFirst().close();
    }

    private void _castToFrontend(NetworkFrame frame, INetwork network) throws CircuitException {
        FrontendCastmode castmode = network.getFrontendCastmode();
        switch (castmode) {
            case selectcast:
                _frontend_selectcast(frame, network);
                break;
            case multicast:
                break;
            case unicast:
                break;
        }
    }


    private void _castToBackend(NetworkFrame frame, INetwork network) throws CircuitException {
        BackendCastmode castmode = network.getBackendCastmode();
        switch (castmode) {
            case selectcast:
                _backend_selectcast(frame, network);
                break;
            case multicast:
                break;
            case unicast:
                break;
        }
    }

    private void _frontend_selectcast(NetworkFrame frame, INetwork network) throws CircuitException {
        String to_person = frame.head("to-person");
        String to_peer = frame.head("to-peer");
        String key = String.format("%s/%s", to_person, to_peer);
        if (!network.hasMemberInFrontend(key)) {
            CJSystem.logging().warn(getClass(), String.format("发送不目标：%s/%s不是本网络成员，侦丢弃。", to_person, to_peer));
            return;
        }
        IEndport endport = endportContainer.openport(key);
        endport.openDownstream().write(frame).close();
    }

    private void _backend_selectcast(NetworkFrame frame, INetwork network) throws CircuitException {
        String to_person = frame.head("to-person");
        String to_peer = frame.head("to-peer");
        String key = String.format("%s/%s", to_person, to_peer);
        if (!network.hasMemberInFrontend(key)) {
            CJSystem.logging().warn(getClass(), String.format("发送不目标：%s/%s不是本网络成员，侦丢弃。", to_person, to_peer));
            return;
        }
        IEndport endport = endportContainer.openport(key);
        endport.openDownstream().write(frame).close();
    }
}