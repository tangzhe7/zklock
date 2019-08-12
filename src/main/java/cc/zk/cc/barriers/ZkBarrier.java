package cc.zk.cc.barriers;

import java.io.IOException;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

public class ZkBarrier extends SyncPrimitive
{
	private int size;
	private String path;

	public ZkBarrier(String address, String rootName, int size, int index)
			throws KeeperException, InterruptedException, IOException
	{
		super(address);
		this.root = rootName;
		this.size = size;

		// Create barrier node
		if (zk != null)
		{
			Stat s = zk.exists(root, false);
			if (s == null)
			{
				zk.create(root, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
		}

		// My node name
		path = rootName + "/" + index;
	}

	public boolean enter() throws KeeperException, InterruptedException
	{
		zk.create(path, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
		while (true)
		{
			synchronized (mutex)
			{
				List<String> list = zk.getChildren(root, true);
				if (list.size() < size)
				{
					mutex.wait();
				}
				else
				{
					return true;
				}
			}
		}
	}

	public boolean leave() throws KeeperException, InterruptedException
	{
		zk.delete(path, 0);
		while (true)
		{
			synchronized (mutex)
			{
				List<String> list = zk.getChildren(root, true);
				if (list.size() > 0)
				{
					mutex.wait();
				}
				else
				{
					return true;
				}
			}
		}
	}

}
