package cj.netos.network.peer;

public interface IPeer {

    void authByPassword(String peer,String person, String password);

    void authByAccessToken(String accessToken);

    void listEventNetwork(IOnerror onerror, IOnopen onopen, IOnmessage onmessage, IOnclose onclose);

    ILogicNetwork listen(String networkName);

    void close();

    String peerName();

}

