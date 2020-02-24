package cj.netos.network.peer;

import cj.netos.network.NetworkFrame;

public interface IOnnotify {
    void onevent(NetworkFrame frame);

    void onerror(NetworkFrame frame);

}
