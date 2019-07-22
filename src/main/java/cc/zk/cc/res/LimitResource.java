package cc.zk.cc.res;



import sun.misc.Contended;

public class LimitResource
{

	@Contended
	private volatile int val;

	public LimitResource()
	{
	}

	public void incrVal()
	{
		val++;
	}

	public int retVal()
	{
		return val;
	};

}
