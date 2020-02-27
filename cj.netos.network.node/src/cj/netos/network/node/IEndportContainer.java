package cj.netos.network.node;

import cj.netos.network.IPrincipal;
import cj.netos.network.ListenMode;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.IClosable;

import java.util.List;

public interface IEndportContainer extends IClosable {
    IEndport openport(IPrincipal principal) throws CircuitException;

    IEndport openport(String principalKey) throws CircuitException;

    List<String> findPersonByPeer(String to_peer);

    List<String> findPeersByPerson(String to_person);

}
