package cj.netos.network.node;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.node.eventloop.EventTask;

/**
 * 抽水泵
 */
public interface IPump {
    void start(INetworkServiceProvider site);

    void close();


    void arriveUpstream(EventTask task);

    void arriveDownstream(EventTask task);
}
