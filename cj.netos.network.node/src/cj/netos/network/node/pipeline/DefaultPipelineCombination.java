package cj.netos.network.node.pipeline;

import cj.netos.network.*;
import cj.netos.network.node.pipeline.valve.CastToEndpointValve;
import cj.netos.network.node.pipeline.valve.CheckSecurityValve;
import cj.netos.network.node.pipeline.valve.DispatchCommandValve;
import cj.netos.network.node.pipeline.valve.ErrorValve;

//注意，该类是单例模式，一个server一个实例
public class DefaultPipelineCombination implements IPipelineCombination {

    @Override
    public synchronized void combine(IPipeline pipeline) throws CombineException {
        INetworkNodePlugin plugin = (INetworkNodePlugin) pipeline.site().getService("$.network.plugin");
        plugin.combine(pipeline);
        if (pipeline.isEmpty()) {
            pipeline.append(new CheckSecurityValve(pipeline.site()));
        }
        pipeline.append(new DispatchCommandValve(pipeline.site()));
        pipeline.append(new CastToEndpointValve(pipeline.site()));
        pipeline.append(new ErrorValve(pipeline.site()));
    }

    @Override
    public synchronized void demolish(IPipeline pipeline) {
        INetworkNodePlugin plugin = (INetworkNodePlugin) pipeline.site().getService("$.network.plugin");
        plugin.demolish(pipeline);
        pipeline.dispose();
    }


}
