package cj.netos.network.node;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.node.eventloop.Task;

/**
 * 抽水泵
 */
public interface IPump {
    void start(INetworkServiceProvider site);

    void close();


    void removeUpstreamKey(String key);

    void removeDownstreamKey(String key);

    void arriveUpstream(Task task);

    void arriveDownstream(Task task);
}
