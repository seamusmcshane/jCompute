package jCompute.Scenario.SAPP.World;

import jCompute.Gui.View.GUISimulationView;

public interface WorldInf
{
	public int getWorldBoundingSquareSize();
	
	public void drawWorld(GUISimulationView simView);
	
	public boolean isInvalidPosition(float x,float y);

	public boolean isValidPosition(float x, float y);
	
}
