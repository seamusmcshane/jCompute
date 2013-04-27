package alifeSim.World;

public class WorldSetupSettings
{
	int worldSize;
	
	int barrierMode;
	
	int barrierScenario;

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
	
}
