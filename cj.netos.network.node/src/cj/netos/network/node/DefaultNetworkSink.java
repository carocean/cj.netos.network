package cj.netos.network.node;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.NetworkFrame;
import cj.netos.network.node.eventloop.Task;
import org.apache.jdbm.DB;

import java.util.List;


//终结点曹，一个网络有多个终结点曹。它拥有一个接收队列
public class DefaultNetworkSink implements INetworkSink {
    INetworkServiceProvider site;
    String person;
    String peer;
    String ownerNetwork;
    List<Object> _storeList;
    DB db;
    IPump pump;

    @Override
    public void close() {
        db.commit();
        db = null;
        site = null;
        pump.removeDownstreamKey(String.format("%s/%s", getKey(), ownerNetwork));
    }

    @Override
    public void open(INetworkServiceProvider site, String ownerNetwork, String person, String peer) {
        this.site = site;
        this.ownerNetwork=ownerNetwork;
        this.person = person;
        this.peer = peer;
        this.db = (DB) site.getService("$.network.db");
        this.pump = (IPump) site.getService("$.network.pump");
        String key = String.format("network.sink://%s/%s", ownerNetwork, getKey());
        List<Object> list = db.getLinkedList(key);
        if (list == null) {
            list = db.createLinkedList(key);
        }
        _storeList = list;
    }

    @Override
    public String getOwnerNetwork() {
        return ownerNetwork;
    }

    @Override
    public synchronized void write(NetworkFrame frame) {
        _storeList.add(frame.toBytes());
        db.commit();
        Task task = new Task(Direction.downstream, this.getKey(), ownerNetwork);
        pump.arriveDownstream(task);
    }

    @Override
    public synchronized NetworkFrame pullFirst() {
        if (_storeList.isEmpty()) {
            return null;
        }
        byte[] bytes =null;
        try {
            bytes = (byte[]) _storeList.get(0);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
        return new NetworkFrame(bytes);
    }

    @Override
    public synchronized void removeFirst() {
        if (_storeList.isEmpty()) {
            return;
        }
        _storeList.remove(0);
        db.commit();
    }

    @Override
    public String getKey() {
        return String.format("%s/%s", person, peer);
    }
}
