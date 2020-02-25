package cj.netos.network.node;

import cj.netos.network.BackendCastmode;
import cj.netos.network.FrontendCastmode;
import cj.netos.network.IPrincipal;
import cj.ultimate.IClosable;

import java.util.List;

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

    String[] listFrontendMembers();

    String[] listBackendMembers();

    boolean hasMemberInFrontend(String key);

    boolean hasMemberInBackend(String key);

    List<String> listFrontendMembersExcept(String endpoint);

    List<String> listBackendMembersExcept(String key);
}
