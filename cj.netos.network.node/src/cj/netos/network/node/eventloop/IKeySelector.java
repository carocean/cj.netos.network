package cj.netos.network.node.eventloop;

import cj.netos.network.node.Direction;
import cj.ultimate.IDisposable;

public interface IKeySelector extends IDisposable {

	int keyCount();

	void removeKey(String key);

	IKey select(String key, Direction direction);

}
