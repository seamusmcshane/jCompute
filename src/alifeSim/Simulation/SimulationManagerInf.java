package alifeSim.Simulation;

import org.newdawn.slick.Graphics;

import alifeSim.Stats.StatManager;

public interface SimulationManagerInf
{	
	public void cleanUp();

	public void doSimulationUpdate();
	
	public StatManager getStatmanger();
	
	public void drawSim(Graphics g, boolean trueDrawing, boolean viewRangeDrawing, boolean viewsDrawing);

	public int getWorldSize();

	public void displayDebug();
}
