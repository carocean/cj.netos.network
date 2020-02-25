package cj.netos.network.node;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.node.eventloop.*;
import cj.netos.network.node.pump.DefaultDownstreamLineCombination;
import cj.netos.network.node.pump.DefaultUpstreamLineCombination;

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
    public void start(INetworkServiceProvider site) {
        INetworkNodeConfig config = (INetworkNodeConfig) site.getService("$.network.config");
        PumpInfo info = config.getPumpInfo();


        INetworkContainer networkContainer = (INetworkContainer) site.getService("$.network.networkContainer");
        IEndportContainer endportContainer = (IEndportContainer) site.getService("$.network.endportContainer");
        IEndpointerContainer endpointerContainer = (IEndpointerContainer) site.getService("$.network.endpointerContainer");

        buildUpstreamEventloop(info, config, networkContainer, endportContainer);
        buildDownstreamEventloop(info, config, endpointerContainer, endportContainer);
    }

    @Override
    public void close() {
        upstreamexe.shutdownNow();
        downstreamexe.shutdownNow();
        upstreamTaskQueue.close();
        downstreamTaskQueue.close();
    }

    @Override
    public Object getService(String name) {
        if ("$.pump.downstream.queue".equals(name)) {
            return downstreamTaskQueue;
        }
        if ("$.pump.upstream.queue".equals(name)) {
            return upstreamTaskQueue;
        }
        return null;
    }

    private void buildUpstreamEventloop(PumpInfo info, INetworkNodeConfig config, INetworkContainer networkContainer, IEndportContainer endportContainer) {
        String queueDir = String.format("%s%sstore%spump%s", config.home(), File.separator, File.separator, File.separator);
        File file = new File(queueDir);
        if (!file.exists()) {
            file.mkdirs();
        }

        upstreamTaskQueue = new DiskStreamTaskQueue();
        upstreamTaskQueue.init(queueDir, "upstream");
        upstreamexe = Executors.newFixedThreadPool(info.upstreamWorkThreadCount());

        ILineCombination combination = new DefaultUpstreamLineCombination(networkContainer, endportContainer);
        upstreamSelector = new DefaultKeySelector(this, combination);
        for (int i = 0; i < info.upstreamWorkThreadCount(); i++) {
            IEventLooper looper = new EventLooper(upstreamSelector, upstreamTaskQueue, info.upstreamQueueWaitTime());
            upstreamexe.submit(looper);
        }
    }

    private void buildDownstreamEventloop(PumpInfo info, INetworkNodeConfig config, IEndpointerContainer endpointerContainer, IEndportContainer endportContainer) {
        String queueDir = String.format("%s%sstore%spump%s", config.home(), File.separator, File.separator, File.separator);
        File file = new File(queueDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        downstreamTaskQueue = new DiskStreamTaskQueue();
        downstreamTaskQueue.init(queueDir, "downstream");
        downstreamexe = Executors.newFixedThreadPool(info.downstreamWorkThreadCount());

        ILineCombination combination = new DefaultDownstreamLineCombination(endpointerContainer, endportContainer);
        downstreamSelector = new DefaultKeySelector(this, combination);
        for (int i = 0; i < info.downstreamWorkThreadCount(); i++) {
            IEventLooper looper = new EventLooper(downstreamSelector, downstreamTaskQueue, info.downstreamQueueWaitTime());
            downstreamexe.submit(looper);
        }
    }

    @Override
    public synchronized void arriveUpstream(EventTask task) {
        upstreamTaskQueue.append(task);
    }

    @Override
    public synchronized void arriveDownstream(EventTask task) {
        downstreamTaskQueue.append(task);
    }
}
