package cj.netos.network.node;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.node.eventloop.*;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DefaultPump implements IPump, INetworkServiceProvider {

    ExecutorService upstreamexe;
    ExecutorService downstreamexe;
    ITaskQueue upstreamTaskQueue;
    IKeySelector upstreamSelector;
    ITaskQueue downstreamTaskQueue;
    IKeySelector downstreamSelector;

    @Override
    public void close() {
        upstreamexe.shutdownNow();
        downstreamexe.shutdownNow();
        upstreamTaskQueue.close();
        downstreamTaskQueue.close();
    }

    @Override
    public Object getService(String name) {
        return null;
    }

    @Override
    public void start(INetworkServiceProvider site) {
        INetworkNodeConfig config = (INetworkNodeConfig) site.getService("$.network.config");
        PumpInfo info = config.getPumpInfo();


        INetworkContainer networkContainer = (INetworkContainer) site.getService("$.network.networkContainer");
        IEndpointerContainer endpointerContainer = (IEndpointerContainer) site.getService("$.network.endpointerContainer");

        buildUpstreamEventloop(info, config, networkContainer, endpointerContainer);
        buildDownstreamEventloop(info, config, networkContainer, endpointerContainer);

    }

    private void buildUpstreamEventloop(PumpInfo info, INetworkNodeConfig config, INetworkContainer networkContainer, IEndpointerContainer endpointerContainer) {
        String queueDir = String.format("%s%squeue%supstream", config.home(), File.separator, File.separator);
        File file = new File(queueDir);
        if (!file.exists()) {
            file.mkdirs();
        }

        upstreamTaskQueue = new DiskStreamTaskQueue(queueDir);
        upstreamTaskQueue.init(info.upstreamQueueFileLength());
        upstreamexe = Executors.newFixedThreadPool(info.upstreamWorkThreadCount());

        ILineCombination combination = new DefaultUpstreamLineCombination(networkContainer, endpointerContainer);
        upstreamSelector = new DefaultKeySelector(this, combination);
        for (int i = 0; i < info.upstreamWorkThreadCount(); i++) {
            IEventLooper looper = new EventLooper(upstreamSelector, upstreamTaskQueue, info.upstreamQueueWaitTime());
            upstreamexe.submit(looper);
        }
    }

    private void buildDownstreamEventloop(PumpInfo info, INetworkNodeConfig config, INetworkContainer networkContainer, IEndpointerContainer endpointerContainer) {
        String queueDir = String.format("%s%squeue%sdownstream", config.home(), File.separator, File.separator);
        File file = new File(queueDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        downstreamTaskQueue = new DiskStreamTaskQueue(queueDir);
        downstreamTaskQueue.init(info.downstreamQueueFileLength());
        downstreamexe = Executors.newFixedThreadPool(info.downstreamWorkThreadCount());

        ILineCombination combination = new DefaultDownstreamLineCombination(networkContainer, endpointerContainer);
        downstreamSelector = new DefaultKeySelector(this, combination);
        for (int i = 0; i < info.downstreamWorkThreadCount(); i++) {
            IEventLooper looper = new EventLooper(downstreamSelector, downstreamTaskQueue, info.downstreamQueueWaitTime());
            downstreamexe.submit(looper);
        }
    }

    @Override
    public void removeUpstreamKey(String key) {
        upstreamSelector.removeKey(key);
    }

    @Override
    public void removeDownstreamKey(String key) {
        downstreamSelector.removeKey(key);
    }

    @Override
    public synchronized void arriveUpstream(Task task) {
        upstreamTaskQueue.append(task);
    }

    @Override
    public synchronized void arriveDownstream(Task task) {
        downstreamTaskQueue.append(task);
    }
}
