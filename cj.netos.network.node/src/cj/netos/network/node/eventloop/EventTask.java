package cj.netos.network.node.eventloop;

import cj.netos.network.node.Direction;

public class EventTask {
    Direction direction;
    String endpoint;
    String network;

    public EventTask(Direction direction, String endpoint, String network) {
        this.direction = direction;
        this.endpoint = endpoint;
        this.network = network;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public String getEndpointKey() {
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
