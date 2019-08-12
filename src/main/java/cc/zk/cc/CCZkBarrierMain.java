package cc.zk.cc;

import java.io.IOException;
import java.util.concurrent.CyclicBarrier;

import org.apache.zookeeper.KeeperException;

import cc.zk.cc.barriers.ZkBarrier;

public class CCZkBarrierMain
{

	private static int size = 3;

	public static void main(String[] args) throws KeeperException, InterruptedException, IOException
	{
		JdkBarrier();
		zkBarrier();
	}

	static void zkBarrier() throws KeeperException, InterruptedException, IOException
	{
		for (int i = 0; i < size; i++)
		{
			final int index = i;
			new Thread(() -> {
				try
				{
					ZkBarrier zk = new ZkBarrier("127.0.0.1:2181", "/barrier", size, index);
					zk.enter();
					System.out.println(index + "........");
					zk.leave();
				}
				catch (KeeperException | InterruptedException | IOException e)
				{
					e.printStackTrace();
				}
			}).start();
		}
	}

	static void JdkBarrier()
	{

		final CyclicBarrier barrier = new CyclicBarrier(size, () -> {
			System.out.println("command\nzk.............");
		});
		for (int i = 0; i < size; i++)
		{
			final int index = i;
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					System.out.println("" + index + "....");
					try
					{
						barrier.await();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}).start();
		}

	}

}
