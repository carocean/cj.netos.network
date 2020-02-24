package cj.netos.network;

public class Sender {
    String person;
    String peer;

    public Sender() {
    }

    public Sender(String person, String peer) {
        this.person = person;
        this.peer = peer;
    }

    public Sender(String endpoint) {
        int pos = endpoint.lastIndexOf("/");
        person = endpoint.substring(0, pos);
        peer = endpoint.substring(pos + 1);
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public String getPeer() {
        return peer;
    }

    public void setPeer(String peer) {
        this.peer = peer;
    }
    public String getKey(){
        return String.format("%s/%s", person, peer);
    }
}
