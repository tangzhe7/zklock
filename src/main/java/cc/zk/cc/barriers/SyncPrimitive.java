package cc.zk.cc.barriers;

import java.io.IOException;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

public class SyncPrimitive implements Watcher
{
	 protected ZooKeeper zk = null;
	static final Object mutex = new Object();
	String root;

	public SyncPrimitive(String address) throws KeeperException, IOException
	{
		if (zk == null)
		{
			zk = new ZooKeeper(address, 3000, this);
		}
	}

	@Override
	public void process(WatchedEvent event)
	{
		synchronized (mutex)
		{
			mutex.notify();
		}

	}
}
