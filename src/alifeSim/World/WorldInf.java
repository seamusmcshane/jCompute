package alifeSim.World;

import alifeSim.Gui.NewSimView;

public interface WorldInf
{
	public int getWorldBoundingSquareSize();
	
	public void drawWorld(NewSimView simView);
	
	public boolean isInvalidPosition(float x,float y);

	public boolean isValidPosition(float x, float y);
	
}
