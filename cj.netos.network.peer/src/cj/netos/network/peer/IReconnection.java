package cj.netos.network.peer;

interface IReconnection {
    void reconnect();

    boolean isForbiddenReconnect();

    void onclose();

    void onopen();

}
