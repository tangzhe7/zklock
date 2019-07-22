package cc.zk.cc.build;

import java.io.IOException;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import sun.misc.Contended;

public class RootSingleBuilder implements Builder<Boolean>
{
	private volatile static Boolean init = Boolean.FALSE;
	private final String root;
	private ZooKeeper zk;
	private final String host;
	private final String port;
	@Contended
	private volatile static RootSingleBuilder builder;

	private RootSingleBuilder(String host, String root, String port)
	{
		this.host = host;
		this.port = port;
		this.root = root;
	}

	public static RootSingleBuilder getBuilder(String host, String root, String port)
	{
		if (builder != null) return builder;
		synchronized (RootSingleBuilder.class)
		{
			if (builder != null)
			{
				return builder;
			}
			builder = new RootSingleBuilder(host, root, port);
		}
		return builder;
	}

	public Boolean build() throws RuntimeException, IOException, KeeperException, InterruptedException
	{
		if (init.equals(Boolean.TRUE)) return Boolean.TRUE;
		synchronized (RootSingleBuilder.class)
		{
			if (init.equals(Boolean.TRUE)) return Boolean.TRUE;
			zk = new ZooKeeper(host + ":" + port, 3000, null);
			Stat stat = zk.exists(root, false);// 不执行 Watcher
			if (stat == null)
			{
				zk.create(root, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			zk.close();
		}
		return Boolean.TRUE;
	}

}
