package cj.netos.network.node.eventloop;

public interface IKey {
    ILine line();

    String key();


    void attachment(Object v);

    Object attachment();
}
