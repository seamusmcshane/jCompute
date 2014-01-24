package alifeSim.Scenario.Math;

import org.newdawn.slick.Graphics;

import alifeSim.Scenario.SAPP.SAPPScenario;
import alifeSim.Simulation.SimulationManagerInf;
import alifeSim.Stats.StatManager;

public class LVSimulationManager implements SimulationManagerInf
{
	LVScenario scenario;
	
	/**
	 * Constructor for SimulationManager.
	*/
	public LVSimulationManager(LVScenario scenario)
	{
		this.scenario = scenario;
		
	}
	
	@Override
	public void cleanUp()
	{
		
	}

	@Override
	public void doSimulationUpdate()
	{
		
	}

	@Override
	public StatManager getStatmanger()
	{
		return null;
	}

	@Override
	public void drawSim(Graphics g, boolean trueDrawing, boolean viewRangeDrawing, boolean viewsDrawing)
	{
		
	}

	@Override
	public int getWorldSize()
	{
		return 0;
	}

	@Override
	public void displayDebug()
	{
	
	}

}
