package cj.netos.network.node.pipeline;

import cj.netos.network.CombineException;
import cj.netos.network.INetworkNodePlugin;
import cj.netos.network.IPipeline;
import cj.netos.network.node.pipeline.valve.CheckSecurityValve;
import cj.netos.network.node.pipeline.valve.DispatchCommandValve;
import cj.netos.network.node.pipeline.valve.ErrorValve;
import cj.netos.network.node.pipeline.valve.UpstreamEndportValve;

public class DefaultPipelineCombination implements cj.netos.network.IPipelineCombination {

    @Override
    public void combine(IPipeline pipeline) throws CombineException {
        INetworkNodePlugin plugin = (INetworkNodePlugin) pipeline.site().getService("$.network.plugin");
        plugin.combine(pipeline);
        if (pipeline.isEmpty()) {
            pipeline.append(new CheckSecurityValve(pipeline.site()));
        }
        pipeline.append(new DispatchCommandValve(pipeline.site()));
        pipeline.append(new UpstreamEndportValve(pipeline.site()));
        pipeline.append(new ErrorValve(pipeline.site()));

    }

    @Override
    public void demolish(IPipeline pipeline) {
        INetworkNodePlugin plugin = (INetworkNodePlugin) pipeline.site().getService("$.network.plugin");
        plugin.demolish(pipeline);
        pipeline.dispose();
    }
}
