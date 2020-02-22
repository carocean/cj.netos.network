package cj.netos.network;

import cj.studio.ecm.net.CircuitException;
import cj.ultimate.IDisposable;

public interface IPipeline extends IDisposable {

    IPrincipal principal();

    void principal(IPrincipal principal);
    /**
     * 管道的附件，也可能空
     *
     * @return
     */
    Object attachment();

    /**
     * 设置附件
     *
     * @param attachment
     */
    void attachment(Object attachment);

    void append(IValve valve);

    void input(NetworkFrame frame) throws CircuitException;

    void nextFlow(NetworkFrame frame, IValve formthis) throws CircuitException;

    void nextError(NetworkFrame frame, Throwable error, IValve formthis) throws CircuitException;

    void remove(IValve valve);

    INetworkServiceProvider site();

    void error(NetworkFrame frame, Throwable e) throws CircuitException;

    boolean isEmpty();

}
