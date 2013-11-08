package alifeSim.Stats;

import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class StatManager
{
	HashMap<String, Stat> map;
	Semaphore statsManagerLock = new Semaphore(1);
	
	public StatManager()
	{
		map = new HashMap<String, Stat>();
	}
	
	// Stat name is the key
	public void addStat(Stat stat)
	{
		statsManagerLock.acquireUninterruptibly();
			map.put(stat.getStatName(), stat);
		statsManagerLock.release();
	}
	
	public Stat getStat(String statName)
	{
		statsManagerLock.acquireUninterruptibly();
			Stat stat = map.get(statName);
		statsManagerLock.release();
		
		return stat;
	}
	
}
