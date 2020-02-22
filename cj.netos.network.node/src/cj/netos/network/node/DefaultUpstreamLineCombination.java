package cj.netos.network.node;

import cj.netos.network.NetworkFrame;
import cj.netos.network.node.eventloop.ILine;
import cj.netos.network.node.eventloop.ILineCombination;
import cj.netos.network.node.eventloop.IReceiver;
import cj.netos.network.node.eventloop.Task;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.net.CircuitException;

public class DefaultUpstreamLineCombination implements ILineCombination {
    INetworkContainer networkContainer;
    IEndpointerContainer endpointerContainer;

    public DefaultUpstreamLineCombination(INetworkContainer networkContainer, IEndpointerContainer endpointerContainer) {
        this.networkContainer = networkContainer;
        this.endpointerContainer = endpointerContainer;
    }

    @Override
    public void combine(ILine line) {
        combineUpstream(line);
    }

    private void combineUpstream(ILine line) {
        IReceiver receiver = new IReceiver() {
            @Override
            public void receive(Task task) throws CircuitException {
                //终结点及终结点槽和网络的槽都是运行时动态创建的，将系统再次启动时它们并不存在，因此不能直接用槽实例，需要采用拉取器方式读取
                //对方如不在线的话，上行可以读取分发，因此会最终存到网络的槽中，而下行则曹为空表明接收的中结点未上线，所以不拉取，等到上线后自然会拉取
                //因此：只需终结点需要实现拉取器
                IEndpointer endpointer = endpointerContainer.endpoint(task.getEndpoint());
                if (endpointer == null || !endpointer.getSinks().containsKey(task.getNetwork())) {
                    ISinkPull sinkPull = endpointerContainer.createSinkPuller(task.getEndpoint(), task.getNetwork());
                    NetworkFrame frame = null;
                    while (true) {
                        frame = sinkPull.pullFirst();
                        if (frame == null) {
                            break;
                        }
                        System.out.println("!!!!!!combineUpstream!!!!" + frame);
                        INetwork network = networkContainer.getNetwork(task.getNetwork());
                        network.cast(frame);
                        //正确消费完就移除
                        sinkPull.removeFirst();
                    }
                    return;
                }

                IEndpointerSink endpointerSink = endpointer.getSinks().get(task.getNetwork());
                NetworkFrame frame = null;
                while (true) {
                    frame = endpointerSink.pullFirst();
                    if (frame == null) {
                        break;
                    }
                    System.out.println("!!!!!!combineUpstream!!!!" + frame);
                    INetwork network = networkContainer.getNetwork(task.getNetwork());
                    network.cast(frame);
                    //正确消费完就移除
                    endpointerSink.removeFirst();
                }
            }

            @Override
            public void error(Task task, Throwable e) {
                CJSystem.logging().error(getClass(), e);
            }
        };
        line.accept(receiver);
    }


}