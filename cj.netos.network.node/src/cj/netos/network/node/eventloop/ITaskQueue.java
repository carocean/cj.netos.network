package cj.netos.network.node.eventloop;

import cj.ultimate.IDisposable;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public interface ITaskQueue extends IDisposable {

    void init(String queueDir, String queueName) ;

    Task selectOne(long longTime, TimeUnit timeUnit);

    void append(Task e);


    void close();
}
