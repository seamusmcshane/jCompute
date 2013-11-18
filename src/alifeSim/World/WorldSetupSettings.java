package alifeSim.World;

public class WorldSetupSettings
{
	int worldSize=1024;
	
	int barrierMode=1;
	
	int barrierScenario=0;

	public int getWorldSize()
	{
		return worldSize;
	}

	public void setWorldSize(int worldSize)
	{
		this.worldSize = worldSize;
	}

	public int getBarrierMode()
	{
		return barrierMode;
	}

	public void setBarrierMode(int barrierMode)
	{
		this.barrierMode = barrierMode;
	}

	public int getBarrierScenario()
	{
		return barrierScenario;
	}

	public void setBarrierScenario(int barrierScenario)
	{
		this.barrierScenario = barrierScenario;
	}
	
	public boolean validate()
	{
		// Only need a world size for the setting to be valid
		if( (worldSize>0) )
		{
			return true;
		}
		
		return false;
	}
	
}
