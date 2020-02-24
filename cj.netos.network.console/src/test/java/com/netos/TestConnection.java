package com.netos;

import cj.netos.network.NetworkFrame;
import cj.netos.network.peer.IConnection;
import cj.netos.network.peer.TcpConnection;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.HashMap;
import java.util.Map;

public class TestConnection {

    public static IConnection getConn() {
        IConnection connection = new TcpConnection(null,null,null);
        Map<String, String> props = new HashMap<>();
        props.put("heartbeat","5");
        connection.connect("tcp", "localhost", 6600, props);
        return connection;
    }

    public static void main(String... args) throws InterruptedException {
        IConnection connection = getConn();
//        authAccessTokenMode(connection);
        authPasswordMode(connection);
//        createNetwork(connection);
        listenNetwork(connection);
        sendMessage(connection);
        Thread.sleep(10000000);
    }

    private static void sendMessage(IConnection connection) throws InterruptedException {
        for (int i = 0; i < 10000000; i++) {
            ByteBuf bb = Unpooled.buffer();
            bb.writeBytes("sssss".getBytes());
            NetworkFrame frame = new NetworkFrame("put /interactive-center/test my/1.0", bb);
            frame.parameter("p1", "这是测试_" + i);
            connection.send(frame);
            Thread.sleep(300L);
        }
    }

    private static void listenNetwork(IConnection connection) {
        NetworkFrame frame = new NetworkFrame("listenNetwork /interactive-center network/1.0");
//        frame.head("transfer-mode", "pull");
//        frame.parameter("isJoinToFrontend", "false");
        connection.send(frame);
    }

    private static void createNetwork(IConnection connection) {
        NetworkFrame frame = new NetworkFrame("createNetwork /network-100 network/1.0");
        frame.parameter("title", "测试100");
//        frame.parameter("frontendCastmode", "multicast");
//        frame.parameter("backendCastmode", "multicast");
        connection.send(frame);
    }

    private static void authPasswordMode(IConnection connection) {
        NetworkFrame frame = new NetworkFrame("auth / network/1.0");
        frame.head("auth-mode", "password");
        frame.parameter("peer", "ipad-0");
        frame.parameter("person", "develop1@gbera.netos");
        frame.parameter("password", "11");
        connection.send(frame);
    }

    public static void authAccessTokenMode(IConnection connection) {
        NetworkFrame frame = new NetworkFrame("auth / network/1.0");
        frame.head("auth-mode", "accessToken");
        frame.parameter("accessToken", "52250BBC89E1AE9BA9EF93700B509415");
        connection.send(frame);
    }

}
