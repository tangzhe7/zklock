package cc.zk.utils;


import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import java.util.concurrent.TimeUnit;

public class ExampleClientLocks
{
	private final InterProcessMutex lock;
	private final LimitedResource resource;
	private final String clientName;
	private final String lockPath;

	public ExampleClientLocks(CuratorFramework client, String lockPath, LimitedResource resource,
			String clientName)
	{
		this.resource = resource;
		this.clientName = clientName;
		this.lockPath=lockPath;
		lock = new InterProcessMutex(client, lockPath);
	}

	public void doWork(long time, TimeUnit unit) throws Exception
	{
		if (!lock.acquire(time, unit))
		{
			throw new IllegalStateException(clientName + " could not acquire the lock");
		}
		try
		{
			System.out.println(clientName + " has the lock "+ " lockPath "+lockPath);
			resource.use();
		}
		finally
		{
			System.out.println(clientName + " releasing the lock");
			lock.release(); // always release the lock in a finally block
		}
	}
}