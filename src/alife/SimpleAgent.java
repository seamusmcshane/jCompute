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
	private int range=10;
	private float range_limit=0; /* Limit = size + range */
	private Circle fov;	
	
	/* Agent Type */
	private int type;
		
	private float speed;
		
	private boolean draw_view = false;
	private boolean collision=false;
	private boolean visible = false;
	
	
	public SimpleAgent(int uid,float x,float y,float size)
	{
		this.uid = uid;
			
		this.speed = (1/size)+1;
		
		this.range = (int) (this.range * size)/2;
								
		addAgentBody(new Vector2f(x,y),size);	
		
		addAgentBrain();
		
		setUpView();			
		
	}
	
	
	/* 
	 * Agent 
	 * 
	 * */
	public void doAgentStep()
	{		
		brain.think();
	}
	
	/* View Range */
	private void setUpView()
	{
		range_limit =  range;
				
		fov = new Circle(body.getBodyPos().getX(),body.getBodyPos().getY(),range_limit);
	}

	/* Debug - Representation of View position */
	private void upDateViewLocation()
	{
		fov.setLocation(body.getBodyPos().getX()-(range_limit),body.getBodyPos().getY()-(range_limit));
	}
	
	/*
	 * 
	 * Init 
	 * 
	 */
	private void addAgentBody(Vector2f pos,float size)
	{
		body = new SimpleAgentBody(pos,size);
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
	public void setVisible(boolean status)
	{
		visible = status;
	}

	public Circle getFieldofView()
	{
		return fov;
	}
	
	/* KNN */
	public Vector2f getPos()
	{
		return body.getBodyPos();
	}

	public double getRange()
	{
		return range_limit;
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
	
	public int getId()
	{
		return uid;
	}
	
}
