package cj.netos.network.node;


import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.NetworkFrame;
import cj.netos.network.TransferMode;
import cj.netos.network.node.eventloop.Task;
import org.apache.jdbm.DB;

import java.util.List;

public class DefaultEndpointerSink implements IEndpointerSink {
    String network;
    TransferMode mode;
    List<Object> _storableList;
    DB db;
    String key;
    private IPump pump;

    @Override
    public String key() {
        return key;
    }

    @Override
    public TransferMode getMode() {
        return mode;
    }

    @Override
    public String getNetwork() {
        return network;
    }

    @Override
    public void close() {
        db.commit();
        pump.removeUpstreamKey(String.format("%s/%s", key, network));
    }

    @Override
    public void open(String key, String network, TransferMode mode, INetworkServiceProvider site) {
        this.key = key;
        this.mode=mode;
        this.network=network;
        this.db = (DB) site.getService("$.network.db");
        this.pump = (IPump) site.getService("$.network.pump");
        String dbname = String.format("endpoint.sink://%s/%s", key, network);
        _storableList = db.getLinkedList(dbname);
        if (_storableList == null) {
            _storableList = db.createLinkedList(dbname);
        }
    }

    @Override
    public void write(NetworkFrame frame) {
        _storableList.add(frame.toBytes());
        db.commit();
        Task task=new Task(Direction.upstream,key(),network);
        pump.arriveUpstream(task);
    }

    @Override
    public synchronized NetworkFrame pullFirst() {
        if (_storableList.isEmpty()) {
            return null;
        }
        byte[] bytes =null;
        try {
            bytes = (byte[]) _storableList.get(0);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
        return new NetworkFrame(bytes);
    }

    @Override
    public synchronized void removeFirst() {
        if (_storableList.isEmpty()) {
            return;
        }
        _storableList.remove(0);
        db.commit();
    }
}