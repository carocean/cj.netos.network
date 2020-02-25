package cj.netos.network.node.eventloop;

import cj.studio.ecm.EcmException;
import cj.ultimate.gson2.com.google.gson.Gson;
import com.leansoft.bigqueue.BigQueueImpl;
import com.leansoft.bigqueue.IBigQueue;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class DiskStreamTaskQueue implements ITaskQueue {
    IBigQueue bigQueue;
    ReentrantLock lock;
    Condition readToWPointerCondition;
    @Override
    public void init(String queueDir, String queueName) {
        lock = new ReentrantLock();
        readToWPointerCondition = lock.newCondition();
        File file = new File(queueDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        try {
            bigQueue = new BigQueueImpl(queueDir, queueName);
        } catch (IOException e) {
            throw new EcmException(e);
        }
    }

    @Override
    public EventTask selectOne(long longTime, TimeUnit timeUnit) {
        byte[] b = null;
        try {
            b = bigQueue.dequeue();
        } catch (IOException e) {
            throw new EcmException(e);
        }
        if (b == null) {
            try {
                lock.lock();
                readToWPointerCondition.await(longTime,timeUnit);
                return selectOne(longTime,timeUnit);
            } catch (InterruptedException e) {
                throw new EcmException(e);
            } finally {
                lock.unlock();
            }
        }
        EventTask e = new Gson().fromJson(new String(b), EventTask.class);
        return e;
    }

    @Override
    public void append(EventTask e) {
        try {
            bigQueue.enqueue(new Gson().toJson(e).getBytes());
        } catch (IOException ex) {
            throw new EcmException(ex);
        }
        try {
            lock.lock();
            readToWPointerCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }


    @Override
    public void close() {
        try {
            bigQueue.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dispose() {
        try {
            bigQueue.removeAll();
        } catch (IOException e) {
            throw new EcmException(e);
        }
    }
}
