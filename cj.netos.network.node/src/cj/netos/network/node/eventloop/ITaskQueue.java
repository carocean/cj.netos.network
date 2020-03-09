package cj.netos.network.node.eventloop;

import cj.ultimate.IDisposable;

import java.util.concurrent.TimeUnit;

public interface ITaskQueue extends IDisposable {

    void init(String queueDir, String queueName) ;

    EventTask selectOne(long longTime, TimeUnit timeUnit);

    void append(EventTask e);



    void close();
}
