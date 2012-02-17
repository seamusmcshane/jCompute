package alife;
/**
 * Used to store the statistics of the current agent.
 * 
 */
public class SimpleAgentStats
{
	/* Agent roaming Speed */
	float roam_speed;
	
	/* Agent Run speed */
	float run_speed;
	
	/* Agent Size */
	float size;
	
	public SimpleAgentStats(int ros, int rus, int sz)
	{
		this.roam_speed = ros;
		
		this.run_speed = rus;
		
		this.size = sz;
	}

}
