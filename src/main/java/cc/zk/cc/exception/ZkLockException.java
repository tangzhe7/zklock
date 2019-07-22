package cc.zk.cc.exception;




public class ZkLockException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public ZkLockException(String e)
	{
		super(e);
	}

	public ZkLockException(Exception e)
	{
		super(e);
	}
}