package cc.zk.cc.lock;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;
import org.apache.zookeeper.data.Stat;

import cc.zk.cc.build.RootSingleBuilder;
import cc.zk.cc.exception.ZkLockException;
import cc.zk.cc.res.*;

public class ZookeeperLock implements Watcher
{
	private ZooKeeper zk;
	private final String splitStr = "_lock_";
	private final String root = "/locks";// 根
	private final String host = "127.0.0.1";
	private final String port = "2181";
	private String waitNode;// 等待前一个锁
	private String myZnode;// 当前锁

	private final int clientId;

	private LimitResource resource;

	private int hasCurNode = 0;

	private int lockSuccess = 0;

	/**
	 * 
	 */
	public ZookeeperLock(int clientId, LimitResource resource)
	{
		this.clientId = clientId;
		this.resource = resource;
		// 创建一个与服务器的连接
		try
		{
			zk = new ZooKeeper(host + ":" + port, 3000, this);
			RootSingleBuilder.getBuilder(host, root, port).build();
		}
		catch (Exception e)
		{
			throw new ZkLockException(e);
		}
	}

	public void process(WatchedEvent event)
	{
		synchronized (this)
		{
			// 其他线程放弃锁的标志
			if (event.getPath().contains(waitNode))
			{
				System.out.println(clientId + " notify by " + event.getPath());
				notifyAll();
			}
			else
			{
				System.out.println("notify error not " + waitNode + " node is " + event.getPath());
			}
		}

	}

	private String findPreviousNode(List<String> subNodes, String myLockName)
	{
		int length;
		String node0Str;
		String pre = subNodes.get(0);
		for (int i = 1; i < subNodes.size(); i++)
		{
			length = Math.min(myLockName.length(), subNodes.get(i).length());
			node0Str = subNodes.get(i).substring(0, length);
			if (myLockName.equals(node0Str))
			{
				return pre;
			}
			pre = subNodes.get(i);
		}
		return null;
	}

	private boolean lock(int clientId)
	{
		try
		{
			String myLockName = clientId + splitStr;
			// 创建临时子节点
			if (hasCurNode == 0)
			{
				myZnode = zk.create(root + "/" + myLockName, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
						CreateMode.EPHEMERAL_SEQUENTIAL);
			}
			// 取出所有子节点
			List<String> subNodes = zk.getChildren(root, false);
			// must be sort,zk EPHEMERAL_SEQUENTIAL that is the last number
			// sort subNodes,find last _
			subNodes.sort((t1, t2) -> {
				// t1<t2 -1
				int val1 = Integer.parseInt(t1.substring(t1.lastIndexOf("_") + 1, t1.length()));
				int val2 = Integer.parseInt(t2.substring(t2.lastIndexOf("_") + 1, t2.length()));
				return val1 - val2;
			});

			if (subNodes.isEmpty())
			{
				throw new ZkLockException("error empty children node");
			}
			String node0Str = subNodes.get(0);
			if (node0Str.startsWith(myLockName))
			{
				// 如果是最小的节点,则表示取得锁
				resource.incrVal();
				System.out.println(clientId + " lock success val " + subNodes.toString());
				// System.out.println(clientId + " lock success val " +resource.retVal());
				return true;
			}
			// findPreChildren
			waitNode = null;
			if ((waitNode = findPreviousNode(subNodes, myLockName)) == null)
			{
				throw new RuntimeException("not found previous nodes");
			}
			// System.out.println(clientId + " wait " + subNodes.toString());
			// repeat
			hasCurNode = 1;
		}
		catch (Exception e)
		{
			throw new ZkLockException(e);
		}
		return false;
	}

	public boolean lock(long waitLockTime, TimeUnit unit)
	{
		if (lockSuccess == 1) return true;
		try
		{
			boolean isLock = false;
			while (zk.getState() == States.CONNECTED && !isLock)
			{
				if (lock(clientId))
				{
					lockSuccess = 1;
					return true;
				}
				waitForLock(unit, waitLockTime);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	private void waitForLock(TimeUnit timeUnit, long waitLockTime) throws InterruptedException, KeeperException
	{
		Stat stat = zk.exists(root + "/" + waitNode, true);// 同时注册监听。
		// 判断比自己小一个数的节点是否存在,如果不存在则无需等待锁,同时注册监听
		if (stat != null)
		{
			synchronized (this)
			{
				wait(timeUnit.toMillis(waitLockTime));
			}
		}
	}

	public void releaseLock(int clientId)
	{
		try
		{
			System.out.println(clientId + " release lock");
			zk.delete(myZnode, -1);
			zk.close();
			myZnode = null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

}
