package cj.netos.network.node;

import cj.netos.network.IPrincipal;
import cj.netos.network.ListenMode;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.IClosable;

public interface IEndportContainer extends IClosable {
    IEndport openport(IPrincipal principal) throws CircuitException;

    IEndport openport(String principalKey) throws CircuitException;
}
