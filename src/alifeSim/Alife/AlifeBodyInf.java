package alifeSim.Alife;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;

import alifeSim.Alife.SimpleAgent.SimpleAgentStats;
import alifeSim.Alife.SimpleAgent.SimpleAgentView;

public interface AlifeBodyInf
{
	
	/**
	 *  Return the body Position 
	 * */
	public Vector2f getBodyPos();

	/**
	 * The eat plant Action, attempts to eat the plant in view.
	 * @param view	
	 * @return boolean */
	public boolean eatPlant(SimpleAgentView view);


	/** 
	 * Returns the agents direction of movement. 
	 * @return float */
	public float getDirection();

	/** 
	 * Fast Body Draw Method - rectangles
	 * @param g Graphics
	 */
	public void drawRectBody(Graphics g);


	/** 
	 * Returns the true size squared as a radius for use in KNN. 
	 * @return float */
	public float getTrueSizeSQRRadius();

	/**
	 * Method getStatsDebugMethod.
	 * @return SimpleAgentStats  */
	public SimpleAgentStats getStatsDebugMethod();
	
	//public void setColor(Color color);

	
}
