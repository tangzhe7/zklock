package cc.zk.cc;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import cc.zk.cc.lock.ZookeeperLock;
import cc.zk.cc.res.LimitResource;

public class CCZkLockMain
{

	private final static int totalNumber = 20;
	// private final static long waitLockTimeout = 30*1000;
	private final static long waitLockTimeout = Integer.MAX_VALUE;
	private final static TestThreadFactory factory = new TestThreadFactory();

	public static void main(String[] args) throws InterruptedException
	{

		ExecutorService executorService = Executors.newFixedThreadPool(totalNumber * 2, factory);
		LimitResource resource = new LimitResource();
		for (int i = 0; i < totalNumber; i++)
		{
			final int clientId = i;
			executorService.execute(() -> {
				ZookeeperLock lock = new ZookeeperLock(clientId, resource);
				boolean ok = lock.lock(waitLockTimeout, TimeUnit.MILLISECONDS);
				if (ok)
				{
					lock.releaseLock(clientId);
				}
				else
				{
					// lock.releaseLock(clientId);
					System.out.println(clientId + " not lock success");
				}
			});
		}
		executorService.shutdown();
		executorService.awaitTermination(10, TimeUnit.MINUTES);
	}

	static class TestThreadFactory implements ThreadFactory
	{

		@Override
		public Thread newThread(Runnable r)
		{
			Thread t = new Thread(r);
			t.setUncaughtExceptionHandler((tt, e) -> {
				e.printStackTrace();
			});
			return t;
		}

	}

}
