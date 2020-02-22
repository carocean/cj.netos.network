package cj.netos.network.node.eventloop;

import cj.netos.network.INetworkServiceProvider;
import cj.netos.network.node.Direction;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultKeySelector implements IKeySelector {
    Map<String, IKey> keys;
    INetworkServiceProvider site;
    ILineCombination lineCombination;

    public DefaultKeySelector(INetworkServiceProvider site, ILineCombination lineCombination) {
        keys = new ConcurrentHashMap<>();
        this.site = site;
        this.lineCombination=lineCombination;
    }

    @Override
    public synchronized IKey select(String key, Direction direction) {
        IKey sk = keys.get(key);
        if (sk != null) {
            return sk;
        }
        ILine line = new DefaultLine(site, key, direction);
        sk = new DefaultKey(line);
        keys.put(key, sk);
        if (lineCombination != null) {
            lineCombination.combine(line);
        }
        return sk;
    }

    @Override
    public int keyCount() {
        return keys.size();
    }

    @Override
    public synchronized void removeKey(String key) {
        IKey k = keys.get(key);
        keys.remove(key);
    }


    @Override
    public void dispose() {
        site = null;
        keys.clear();
    }

}
