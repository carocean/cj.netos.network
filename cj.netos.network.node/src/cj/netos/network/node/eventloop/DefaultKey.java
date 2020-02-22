package cj.netos.network.node.eventloop;

public class DefaultKey implements IKey {
    ILine line;
    Object attachment;
    public DefaultKey(ILine line) {
        this.line = line;

    }
    @Override
    public ILine line() {
        return line;
    }

    @Override
    public String key() {
        return line.key();
    }
    @Override
    public void attachment(Object v) {
        attachment = v;
    }
    @Override
    public Object attachment() {
        return attachment;
    }

}
