package cc.zk.utils;

import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LockingExampleMain
{
	private static final int QTY = 20;

	private static final String PATH = "/examples/locks";

	private final static String host = "127.0.0.1";
	
	private final static int waitLockTime = Integer.MAX_VALUE;

	public static void main(String[] args) throws Exception
	{
		// all of the useful sample code is in ExampleClientThatLocks.java

		// FakeLimitedResource simulates some external resource that can only be access
		// by one process at a time
		final LimitedResource resource = new LimitedResource();
		ExecutorService executorService = Executors.newFixedThreadPool(QTY*2);
		for (int i = 0; i < QTY; ++i)
		{
			final int index = i;
			executorService.submit(() -> {
				CuratorFramework client = CuratorFrameworkFactory.newClient(host,
						new ExponentialBackoffRetry(/* 重试间隔时间 */waitLockTime, /* 重试次数 */3));
				try
				{
					client.start();
					ExampleClientLocks example = new ExampleClientLocks(client, PATH, resource,
							"Client " + index);
					example.doWork(waitLockTime, TimeUnit.SECONDS);
				}
				catch (InterruptedException e)
				{
					Thread.currentThread().interrupt();
				}
				catch (Exception e)
				{
					e.printStackTrace();
					// log or do something
				}
				finally
				{
					// close client
					CloseableUtils.closeQuietly(client);
				}
			});
		}

		executorService.shutdown();
		executorService.awaitTermination(10, TimeUnit.MINUTES);
	}
}