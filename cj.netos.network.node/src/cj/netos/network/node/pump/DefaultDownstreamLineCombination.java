package cj.netos.network.node.pump;

import cj.netos.network.node.IEndpointerContainer;
import cj.netos.network.node.IEndportContainer;
import cj.netos.network.node.eventloop.ILine;
import cj.netos.network.node.eventloop.ILineCombination;

public class DefaultDownstreamLineCombination implements ILineCombination {
    private final IEndpointerContainer endpointerContainer;
    private final IEndportContainer endportContainer;

    @Override
    public void combine(ILine line) {
        line.accept(new SendFrameToEndpointer(endpointerContainer, endportContainer));
    }

    public DefaultDownstreamLineCombination(IEndpointerContainer endpointerContainer, IEndportContainer endportContainer) {
        this.endpointerContainer = endpointerContainer;
        this.endportContainer = endportContainer;
    }
}
