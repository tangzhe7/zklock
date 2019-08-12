package cc.zk.cc;

import java.io.IOException;

import org.apache.zookeeper.KeeperException;

import cc.zk.cc.queue.ZkQueue;

public class CCZkQueueMain
{
	static int size = 3;

	public static void main(String[] args) throws KeeperException, IOException, InterruptedException
	{
		ZkQueue queue = new ZkQueue("127.0.0.1:2181", "/queue");
		for (int i = 0; i < size; i++)
		{
			System.out.println("push queue val "+i);
			queue.produce(i);
			queue.consume();
			System.out.println("consume queue val "+i);
		}
	}

}
