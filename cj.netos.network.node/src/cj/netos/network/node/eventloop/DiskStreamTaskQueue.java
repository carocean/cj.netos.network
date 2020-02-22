package cj.netos.network.node.eventloop;

import cj.netos.network.node.stream.DiskStream;
import cj.studio.ecm.EcmException;
import cj.ultimate.gson2.com.google.gson.Gson;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class DiskStreamTaskQueue implements ITaskQueue {
    DiskStream diskStream;
    String dbHome;
    long dataFileLength;
    ReentrantLock lock;
    Condition readToWPointerCondition;

    public DiskStreamTaskQueue(String dbHome) {
        this.dbHome = dbHome;
    }

    @Override
    public void init(long capacity) {
        lock = new ReentrantLock();
        readToWPointerCondition = lock.newCondition();
        this.dataFileLength = capacity;
        try {
            diskStream = new DiskStream(dbHome, dataFileLength);
        } catch (IOException e) {
            throw new EcmException(e);
        }
    }

    @Override
    public Task selectOne(long longTime, TimeUnit timeUnit) {
        byte[] b = null;
        try {
            b = diskStream.read();
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
        Task e = new Gson().fromJson(new String(b), Task.class);
        return e;

    }

    @Override
    public void append(Task e) {
        try {
            diskStream.write(new Gson().toJson(e).getBytes());
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
    public void dispose() {
        try {
            this.diskStream.close();
        } catch (IOException e) {
            throw new EcmException(e);
        }
    }

    @Override
    public void close() {
        try {
            diskStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
