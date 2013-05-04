package testingApp;
import java.util.Iterator;
import java.util.Random;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;


public class SimpleAgent
{
	// Agent Body
	public SimpleAgentBody body;
	
	
	private int id;
	
	private Iterable<SimpleAgent> viewList; /* List of Agents in view */
	private Iterator iterator;
	

	/* Agent View Range */
	private int range=5;
	private float range_limit=0; /* Limit = size + range */
	private Circle field;	
	
	/* Agent Type */
	private int type;
		
	private float speed;
		
	private boolean high_lighted = false;
	private boolean collision=false;
	private boolean visible = false;
	private int viewCount=0;
	
	/* Movement */
	Random d;
	Vector2f xy;
	private int moves=0;
	private int max_dir=100;

	private SimpleAgent nearestAgent;
	
	public SimpleAgent(int id,float x,float y,float size,int type)
	{
		this.id = id;
			
		this.speed = (1/size)+1;
		
		this.range = (int) (this.range * size)/2;
				
		this.type = type;
		
		d = new Random();
		
		max_dir = d.nextInt(50)+1;

		
		xy = polarToCar(speed,d.nextInt(360));
		
		createBody(new Vector2f(x,y),size);	
		
		setUpView();
			
	}

	
	/*
	 * 
	 *  AI
	 */
	
	public void think()
	{
		
		if(this.id==-1)
		{
			return;
		}
		
		/* TODO if nothing in range and traveled for a while */		
		if(moves > max_dir && (nearestAgent == null) )
		{
			moves=0;
			//xy = polarToCar(1,d.nextInt(360)+1);
						
			//xy = carToPolar(mainApp.mouse_pos.x,mainApp.mouse_pos.y);
			
			//xy = polarToCar(1,xy.y);
			
			//xy = 
		}

		this.collision=false;

		
		if(nearestAgent!=null)
		{

			this.collision=true;
			
			//System.out.println("Distance " + nearestAgent.getDistance());

			
				if(nearestAgent.getType() != this.getType() && this.type == 1)
				{				
					xy = carToPolar(nearestAgent.getPos().getX(),nearestAgent.getPos().getY());
				
					xy = polarToCar(speed,-xy.x );
					
					/*if(this.high_lighted == true)
					System.out.println("Fire 1");*/

				}
				
				/* TODO not correct */
				if(nearestAgent.getType() != this.getType() && this.type == 2)
				{				
					xy = carToPolar(nearestAgent.getPos().getX(),nearestAgent.getPos().getY());
				
					xy = polarToCar(speed,xy.x );
					
					/*if(this.high_lighted == true)
					System.out.println("Fire 2");*/
				}		
	
				/* TODO not correct */
				if(nearestAgent.getType() != this.getType() && this.type == 3)
				{				
					xy = carToPolar(nearestAgent.getPos().getX(),nearestAgent.getPos().getY());
				
					xy = polarToCar(speed,xy.x/2 );
					
					/*if(this.high_lighted == true)
					System.out.println("Fire 2");*/
				}					

				/* TODO not correct */
				if(nearestAgent.getType() != this.getType() && this.type == 4)
				{				
					xy = carToPolar(nearestAgent.getPos().getX(),nearestAgent.getPos().getY());
				
					xy = polarToCar(speed,-xy.x/2 );
					
					/*if(this.high_lighted == true)
					System.out.println("Fire 2");*/
				}	
				
				/*if(temp.getType() == this.getType() && this.type == 1 && !temp.equals(this))
				{				
					xy = carToPolar(temp.getPos().getX(),temp.getPos().getY());
				
					xy = polarToCar(1,xy.x );
					
					moves--;
					
					if(this.high_lighted == true)
					System.out.println("Fire 3");

				}*/
				
				/*if(this.high_lighted == true)
				{
					System.out.println("Id " + id + " ViewCount " + viewCount + " Type " + type + " Moves " + moves);
				}*/
				
		}
		//max_dir = d.nextInt(250);
	
		
		//xy = new Vector2f(150,150);
		/* Reverse if stuck against wall */
		if(!body.move(xy))
		{
			xy = carToPolar(xy.x, xy.y);
								
			xy = polarToCar(-xy.x,xy.y);

			body.move(xy);

		}
	
		moves++;

		upDateViewLocation();
	}


	
	/* View Range */
	private void setUpView()
	{
		range_limit =  range;
				
		field = new Circle(body.getBodyPos().getX(),body.getBodyPos().getY(),range_limit);
	}

	/* Representation of View position */
	private void upDateViewLocation()
	{
		field.setLocation(body.getBodyPos().getX()-(range_limit),body.getBodyPos().getY()-(range_limit));
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
			iterator =  tempList.iterator();

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
	private void createBody(Vector2f pos,float size)
	{
		body = new SimpleAgentBody(pos,size,type);
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
	
	public int getType()
	{
		return type;
	}

	public double getRange()
	{
		return range_limit;
	}

	public void updateNearestAgentKD(SimpleAgent nearestAgent)
	{
		this.nearestAgent = nearestAgent;
	}
	
	/* Debug */	
	public void setHighlighted(boolean highlight)
	{
		high_lighted = highlight;
	}
	
	public void drawViewRange(Graphics g)
	{
		if(high_lighted && !collision)
		{ 
			g.setColor(Color.white);
			g.draw(field);
		}
		else if(collision)
		{
			g.setColor(Color.yellow);
			g.draw(field);

		}
		
	}
	
	public void setDebugPos(Vector2f pos)
	{
		body.setDebugPos(pos);
		upDateViewLocation();
	}

}
