package cj.netos.network.node;

import cj.netos.network.BackendCastmode;
import cj.netos.network.FrontendCastmode;
import cj.netos.network.IPrincipal;
import cj.ultimate.IClosable;

import java.util.Set;

//缓冲LRU前置成员和后置成员
public interface INetwork extends IClosable {
    String getName();

    String getTitle();

    FrontendCastmode getFrontendCastmode();

    BackendCastmode getBackendCastmode();


    void addMember(IPrincipal principal, boolean joinToFrontend);

    void removeMember(IPrincipal principal);

    int frontendMemberCount();

    int backendMemberCount();

    Set<String> listFrontendMembers();

    Set<String> listBackendMembers();

    boolean hasMemberInFrontend(String key);

    boolean hasMemberInBackend(String key);

}
