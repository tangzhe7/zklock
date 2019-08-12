package cc.zk.cc.queue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

import cc.zk.cc.barriers.SyncPrimitive;

public class ZkQueue extends SyncPrimitive
{
	private String root;

	private Object mutex = new Object();

	public ZkQueue(String address, String name) throws KeeperException, IOException, InterruptedException
	{
		super(address);
		this.root = name;
		// Create ZK node name
		if (zk != null)
		{
			Stat s = zk.exists(root, false);
			if (s == null)
			{
				zk.create(root, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
		}
	}

	public boolean produce(int i) throws KeeperException, InterruptedException
	{
		ByteBuffer b = ByteBuffer.allocate(4);
		byte[] value;
		b.putInt(i);
		value = b.array();
		zk.create(root + "/element", value, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
		return true;
	}

	public int consume() throws KeeperException, InterruptedException
	{
		int retvalue = -1;
		Stat stat = null;

		while (true)
		{
			synchronized (mutex)
			{
				List<String> list = zk.getChildren(root, true);
				if (list.isEmpty())
				{
					System.out.println("Going to wait");
					mutex.wait();
				}
				else
				{
					String minS = null;
					Integer min = new Integer(list.get(0).substring(7));
					for (String s : list)
					{
						Integer tempValue = new Integer(s.substring(7));
						if (tempValue <= min)
						{
							min = tempValue;
							minS = s;
						}
					}
					//System.out.println("Temporary value: " + root + "/" + minS);
					byte[] b = zk.getData(root + "/" + minS, false, stat);
					//如果不存在,会抛出异常,并发消费情况
					zk.delete(root + "/" + minS, 0);
					ByteBuffer buffer = ByteBuffer.wrap(b);
					retvalue = buffer.getInt();
					return retvalue;
				}
			}
		}
	}

}
