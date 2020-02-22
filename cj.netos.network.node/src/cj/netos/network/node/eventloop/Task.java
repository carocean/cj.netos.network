package cj.netos.network.node.eventloop;

import cj.netos.network.node.Direction;

public class Task {
    Direction direction;
    String endpoint;
    String network;

    public Task(Direction direction, String endpoint, String network) {
        this.direction = direction;
        this.endpoint = endpoint;
        this.network = network;
    }

    public Task(Direction direction, String person, String peer, String network) {
        this.direction = direction;
        this.endpoint = String.format("%s/%s", person, peer);
        this.network = network;
    }
    public String getKey(){
        return String.format("%s/%s", endpoint, network);
    }
    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    @Override
    public String toString() {
        return "Task{" +
                "direction=" + direction +
                ", endpoint='" + endpoint + '\'' +
                ", network='" + network + '\'' +
                '}';
    }
}
