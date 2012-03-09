/*
 * 
 */
package alife;
import java.util.Iterator;
import java.util.Random;

import org.lwjgl.input.Mouse;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;


public class SimpleAgent
{
	// Agent Body
	public SimpleAgentBody body;	
	
	public SimpleAgentBrain brain;
		
	/* Agent Unique ID */
	private int uid;
	
	/* Agent View Range */
	private Circle fov;	
				
	/* View rannge drawing */
	private boolean draw_view = false;
	
	private boolean collision=false;
	
	private boolean removal_tag=false;
			
	public SimpleAgent(int uid,float x,float y,SimpleAgentStats stats)
	{
		this.uid = uid;
											
		addAgentBody(new Vector2f(x,y),stats);	
		
		addAgentBrain();
		
		setUpView();		
		
	}
	
	
	/* 
	 * Agent 
	 * 
	 */
	public void doAgentStep()
	{		
		brain.think();
	}
	
	/* View Range */
	private void setUpView()
	{				
		fov = new Circle(body.getBodyPos().getX(),body.getBodyPos().getY(),body.stats.getView_range());
	}

	/* Debug - Representation of View position */
	private void upDateViewLocation()
	{
		fov.setLocation(body.getBodyPos().getX()-(body.stats.getView_range()),body.getBodyPos().getY()-(body.stats.getView_range()));
	}
	
	/*
	 * 
	 * Init 
	 * 
	 */
	private void addAgentBody(Vector2f pos,SimpleAgentStats stats)
	{
		body = new SimpleAgentBody(pos,stats);
	}

	private void addAgentBrain()
	{
		brain = new SimpleAgentBrain(body);
	}
	
	/*
	 * 
	 * Graphics 
	 * 
	 */
	public Circle getFieldofView()
	{
		return fov;
	}
	
	/* Debug */	
	public void setViewDrawing(boolean setting)
	{
		draw_view = setting;
	}
	
	public void setDebugPos(Vector2f pos)
	{
		body.setDebugPos(pos);		
	}
	
	public void drawViewRange(Graphics g)
	{
	
		upDateViewLocation();
		
		if(draw_view && !collision)
		{ 
			g.setColor(Color.white);
			g.draw(fov);
		}
		else if(collision)
		{
			g.setColor(Color.yellow);
			g.draw(fov);

		}
		
	}
	
	public void setId(int id)
	{
		this.uid = id;
	}
	
	public int getId()
	{
		return uid;
	}
	
	
	/* Used by simulation loop to remove an agent */
	public void setRemovalFlag(boolean remove)
	{
		removal_tag = remove;
	}
		
	public boolean remove()
	{
		return removal_tag;
	}
	
	
}
