package cc.zk.utils;


import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simulates some external resource that can only be access by one process at a
 * time
 */
public class LimitedResource
{
	private final AtomicBoolean inUse = new AtomicBoolean(false);

	public void use() throws InterruptedException
	{
		// in a real application this would be accessing/manipulating a shared resource

		if (!inUse.compareAndSet(false, true))
		{
			throw new IllegalStateException("Needs to be used by one client at a time");
		}

		try
		{
			Thread.sleep((long) (3000));
		}
		finally
		{
			inUse.set(false);
		}
	}
}