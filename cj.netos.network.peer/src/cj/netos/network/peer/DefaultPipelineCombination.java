package cj.netos.network.peer;

import cj.netos.network.*;
import cj.studio.ecm.net.CircuitException;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultPipelineCombination implements IPipelineCombination {
    Map<String, ILogicNetwork> networkMap;
    IOnnotify onnotify;

    public DefaultPipelineCombination(IOnnotify onnotify) {
        networkMap = new ConcurrentHashMap<>();
        this.onnotify = onnotify;
    }

    @Override
    public void combine(IPipeline pipeline) throws CombineException {
        pipeline.append(new IValve() {
            @Override
            public void flow(NetworkFrame frame, IPipeline pipeline) throws CircuitException {
                if (!"NETWORK/1.0".equals(frame.protocol())) {
                    _flowSystem(frame, pipeline);
                    return;
                }
                _flowCustom(frame, pipeline);
            }

            private void _flowCustom(NetworkFrame frame, IPipeline pipeline) {
                String network = frame.rootName();
                ILogicNetwork nw = networkMap.get(network);
                if (nw != null) {
                    DefaultLogicNetwork defaultLogicNetwork = (DefaultLogicNetwork) nw;
                    defaultLogicNetwork.fireOnmessage(frame);
                    return;
                }
                if (onnotify != null) {
                    onnotify.onevent(frame);
                }

            }

            private void _flowSystem(NetworkFrame frame, IPipeline pipeline) {
                if (onnotify == null) {
                    return;
                }
                switch (frame.command()) {
                    case "error":
                        onnotify.onerror(frame);
                        break;
                    default:
                        onnotify.onevent(frame);
                        break;
                }
            }

            @Override
            public void nextError(NetworkFrame frame, Throwable error, IPipeline pipeline) throws CircuitException {
                //不支持
            }
        });
    }

    @Override
    public void demolish(IPipeline pipeline) {

    }

    public void addLogicNetwork(ILogicNetwork lnetwork) {
        networkMap.put(lnetwork.getNetwork(), lnetwork);
    }

    public void removeLogicNetwork(String network) {
        networkMap.remove(network);
    }

    public ILogicNetwork localNetwork(String networkName) {
        return networkMap.get(networkName);
    }

    public Set<String> enumLocalNetwork() {
        return networkMap.keySet();
    }
}