package cj.netos.network.peer;

interface IReconnection {
    void reconnect();

    boolean isForbiddenReconnect();

    void accept(IOnreconnection onreconnection);

}
