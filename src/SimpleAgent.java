import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;


public class SimpleAgent
{
	// Agent Body
	private SimpleAgentBody body;
	
	/* Agent View Range */
	
	private Iterable<SimpleAgent> viewList; /* List of Agents in view */
	private Iterator iterator;
	
	private int range=50;
	private float range_limit=0; /* Limit = size + range */
	private Rectangle field;	
	
	/* Agent Type */
	private int type;
	
	private float world_size;
	
	//private float x,y;
	private float x_dir=1,y_dir=1;
	
	private float body_size;
	
	private float next_x,next_y;
	
	private boolean high_lighted = false;
	private boolean visible = false;
	private int viewCount=0;
	
	/* Movement */
	Random d;
	Vector2f xy;
	private int moves=0;
	private int max_dir=50;
	
	public SimpleAgent(float x,float y,float body_size,float world_size,int type)
	{
		this.body_size=body_size;
		
		this.world_size=world_size;	
		
		this.type = type;
		
		d = new Random();
		
		xy = polarToCar(1,d.nextInt(360));
		
		createBody(new Vector2f(x,y));	
		
		setUpView();
			
	}

	
	/*
	 * 
	 *  AI
	 */
	
	public void think()
	{
		
		if(moves>max_dir)
		{
			moves=0;
			xy = polarToCar(1,d.nextInt(360));
			
			//xy = carToPolar(mainApp.mouse_pos.x,mainApp.mouse_pos.y);
			
			//xy = polarToCar(1,xy.y);
			
			//xy = 
			
			//max_dir = d.nextInt(5);
		}
		
		moves++;
		

		
		body.move(xy);
				
		upDateViewLocation();
	}

	/* View Range */
	private void setUpView()
	{
		range_limit = body_size + range;
		field = new Rectangle(body.getBodyPos().getX()-(range_limit/2),body.getBodyPos().getY()-(range_limit/2),this.range_limit,this.range_limit);
	}
	
	private void upDateViewLocation()
	{
		field.setLocation(body.getBodyPos().getX()-(range_limit/2),body.getBodyPos().getY()-(range_limit/2));
	}
	
	public void updateAgentView(Iterable<SimpleAgent> tempList)
	{
		viewList = tempList;
		
		if(tempList!=null)
		{

			iterator =  tempList.iterator();
			viewCount=0;
			
			while(iterator.hasNext())
			{
				((SimpleAgent) iterator.next()).setVisible(true);
				viewCount++;
			}
		
			//System.out.println(viewCount);
			/*if(temp.equals(tempList.get()))
			{
				if(tempList.getDistance()<temp.getRange())
				{
					temp.setHighlighted(true);
				}
				else
				{
					temp.setHighlighted(false);
				}

			}
			else
			{
				temp.setHighlighted(false);

			}*/
		}
	}
	
	/*
	 * 
	 *  World Physics
	 * 
	 */


	
	/* World Physics checks */
	private boolean canIMoveHere(float x,float y)
	{
		
		/* Check World Boundaries */
		if(World.isBondaryWall(x, y))
		{
			return false;
		}
		
		return true;
	}
	
	/*
	 * 
	 * Init 
	 * 
	 */
	private void createBody(Vector2f pos)
	{
		body = new SimpleAgentBody(pos,body_size,type);
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
	
	public void drawAgent(Graphics g)
	{
		if(visible)
		{
			body.drawBody(g);			
		}
	}

	/* KNN */
	public Rectangle getBodyBounds()
	{
		return body.getBodyBounds();
	}
	
	public Rectangle getFieldofView()
	{
		return field;
	}
	
	/* KNN */
	public Vector2f getPos()
	{
		return body.getBodyPos();
	}
	
	/* 
	 * 
	 * Helpers 
	 * 
	 */
	private Vector2f polarToCar(float r,float theta)
	{
		float x = (float) (r * Math.cos(theta));
		float y = (float) (r * Math.sin(theta));
		
		return new Vector2f(x,y);
	}
	
	private Vector2f carToPolar(float x, float y)
	{
		float r = (float) Math.sqrt((x*x)+(y*y));
		
		float theta = (float) Math.atan2(y, x);
		
		/* Polar Vector */
		return new Vector2f(r,theta);
	}
	
	/* Debug */
	
	public void setHighlighted(boolean highlight)
	{
		high_lighted = highlight;
	}
	
	public void drawViewRange(Graphics g)
	{
		if(high_lighted)
		{ 
			g.setColor(Color.white);
			g.draw(field);
		}
	}


	public double getRange()
	{
		// TODO Auto-generated method stub
		return range_limit;
	}
}
