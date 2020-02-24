package cj.netos.network.peer;

import cj.netos.network.BackendCastmode;
import cj.netos.network.FrontendCastmode;

import java.util.Set;

public interface IPeer {

    void authByPassword(String peer,String person, String password);

    void authByAccessToken(String accessToken);

    ILogicNetwork listen(String networkName,boolean isJoinToFrontend);

    ILogicNetwork localNetwork(String networkName);

    Set<String> enumLocalNetwork();

    void createNetwork(String networkName, String title, FrontendCastmode frontendCastmode, BackendCastmode backendCastmode);

    void removeNetwork(String networkName);

    void listNetwork();

    void close();

    void removeLogicNetwork(ILogicNetwork network);

    void viewServer();

}

