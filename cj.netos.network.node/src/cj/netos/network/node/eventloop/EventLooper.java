package cj.netos.network.node.eventloop;

import cj.studio.ecm.CJSystem;
import cj.studio.ecm.net.CircuitException;

import java.util.concurrent.TimeUnit;

public class EventLooper implements IEventLooper {
    IKeySelector selector;
    ITaskQueue queue;
    long waitTIme;
    public EventLooper(IKeySelector selector, ITaskQueue queue,long waitTIme) {
        this.selector = selector;
        this.queue = queue;
        this.waitTIme=waitTIme;
    }

    @Override
    public Task call() throws Exception {
        while (!Thread.interrupted()) {
            Task task = queue.selectOne(waitTIme, TimeUnit.MILLISECONDS);
//            System.out.println("发现-------"+task);
            IKey key = null;
            try{
                key = selector.select(task.getKey(),task.direction);
            }catch (Throwable e){
                selector.removeKey(task.getKey());
                CJSystem.logging().warn(getClass(),e.getMessage());
                continue;
            }
            synchronized (key.key()) {// 让同一个管道的事件按序执行
//                System.out.println("同步-------");
                try {
                    key.line().input(task);
//                    System.out.println("完成-------"+task);
                } catch (Throwable e) {
                    e.printStackTrace();
                    CircuitException ce = CircuitException.search(e);
                    if (ce != null) {
                        e = ce;
                    }
                    try {
                        key.line().error(task, e);
                    } catch (Throwable e2) {
                        selector.removeKey(task.getKey());
                        CJSystem.logging().error(getClass(), e2);
                    }
                }
            }

        }

        return null;
    }


}
