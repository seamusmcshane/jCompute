package alifeSim.World;

import org.newdawn.slick.Graphics;

public interface WorldInf
{
	public int getWorldBoundingSquareSize();
	
	public void drawWorld(Graphics g);
	
	public boolean isInvalidPosition(float x,float y);

	public boolean isValidPosition(float x, float y);
	
}
