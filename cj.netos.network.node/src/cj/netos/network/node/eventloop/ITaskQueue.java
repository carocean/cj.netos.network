package cj.netos.network.node.eventloop;

import cj.ultimate.IDisposable;

import java.util.concurrent.TimeUnit;

public interface ITaskQueue extends IDisposable {

    Task selectOne(long longTime, TimeUnit timeUnit);

    void append(Task e);


    void init(long capacity);

    void close();
}
