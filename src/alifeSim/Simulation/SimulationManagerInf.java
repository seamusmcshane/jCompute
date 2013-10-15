package alifeSim.Simulation;

import org.newdawn.slick.Graphics;

public interface SimulationManagerInf
{	
	public void cleanUp();

	public void doSimulationUpdate();
	
	public void drawSim(Graphics g, boolean trueDrawing, boolean viewRangeDrawing, boolean viewsDrawing);

	public int getWorldSize();

	public void displayDebug();
}
