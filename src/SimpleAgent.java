import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import org.khelekore.prtree.DistanceResult;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;


public class SimpleAgent
{
	// Agent Body
	private SimpleAgentBody body;
	
	/* Agent View Range */
	
	private int id;
	
	private Iterable<SimpleAgent> viewList; /* List of Agents in view */
	private Iterator iterator;
	
	//DistanceResult<SimpleAgent> nearestAgent;

	
	private int range=5;
	private float range_limit=0; /* Limit = size + range */
	private Rectangle field;	
	
	/* Agent Type */
	private int type;
	
	private float world_size;
	
	//private float x,y;
	private float x_dir=1,y_dir=1;
	
	private float body_size;
	private float speed;
	
	private float next_x,next_y;
	
	private boolean high_lighted = false;
	private boolean visible = false;
	private int viewCount=0;
	
	/* Movement */
	Random d;
	Vector2f xy;
	private int moves=0;
	private int max_dir=100;

	private SimpleAgent nearestAgent;
	
	public SimpleAgent(int id,float x,float y,float body_size,float world_size,int type)
	{
		this.id = id;
		
		this.body_size=body_size;
		
		this.speed = (1/body_size)+type;
		
		this.range = (int) (this.range * this.body_size)/2;
		
		this.world_size=world_size;	
		
		this.type = type;
		
		d = new Random();
		
		max_dir = d.nextInt(50)+1;

		
		xy = polarToCar(speed,d.nextInt(360));
		
		createBody(new Vector2f(x,y));	
		
		setUpView();
			
	}

	
	/*
	 * 
	 *  AI
	 */
	
	public void think()
	{
		
		/* TODO if nothing in range and traveled for a while */		
		if(moves > max_dir && (nearestAgent == null) )
		{
			moves=0;
			xy = polarToCar(1,d.nextInt(360)+1);
						
			//xy = carToPolar(mainApp.mouse_pos.x,mainApp.mouse_pos.y);
			
			//xy = polarToCar(1,xy.y);
			
			//xy = 
		}

		
		if(nearestAgent!=null)
		{

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
	
		/* Reverse if stuck against wall */
		if(!body.move(xy))
		{
			xy = carToPolar(xy.x, xy.y);
								
			xy = polarToCar(-xy.x,xy.y);

			body.move(xy);

		}
	
		//moves++;

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
			iterator =  tempList.iterator();

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

	public int getType()
	{
		return type;
	}

	public double getRange()
	{
		// TODO Auto-generated method stub
		return range_limit;
	}

	/* Alternative Single Agent Result */
	/*public void updateNearestAgent(DistanceResult<SimpleAgent> nearestAgent)
	{	
		this.nearestAgent = nearestAgent;		
	}*/

	
	public void think2()
	{
		
		/* TODO if nothing in range and traveled for a while */
		
		/*if(moves>max_dir)
		{
			moves=0;
			xy = polarToCar(1,d.nextInt(360)+1);
			
			//xy = carToPolar(mainApp.mouse_pos.x,mainApp.mouse_pos.y);
			
			//xy = polarToCar(1,xy.y);
			
			//xy = 
		}*/	

		
			if(viewList!=null)
			{
			
				System.out.println("ID " + id);

				
				if(viewList.iterator().hasNext())
				{
					SimpleAgent temp = 	viewList.iterator().next();
							
					/*if(this.high_lighted == true)
					{
						temp.setVisible(true);
					}
					else
					{
						this.setVisible(false);
					}*/
					
					//temp.setHighlighted(false);
					//this.setHighlighted(true);
					if(temp.getType() != this.getType() && this.type == 2)
					{				
						xy = carToPolar(temp.getPos().getX(),temp.getPos().getY());
					
						xy = polarToCar(speed,-xy.x );
						moves--;
						
						/*if(this.high_lighted == true)
						System.out.println("Fire 1");*/

					}
					
					if(temp.getType() != this.getType() && this.type == 1)
					{				
						xy = carToPolar(temp.getPos().getX(),temp.getPos().getY());
					
						xy = polarToCar(speed,-xy.x );
						
						moves--;

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
			}
		//max_dir = d.nextInt(250);
	
		

		
		if(!body.move(xy))
		{
			xy = carToPolar(xy.x, xy.y);
					
			
			xy = polarToCar(-xy.x,xy.y);

			
			body.move(xy);
		}
		moves++;
	
		upDateViewLocation();
	}


	public void updateNearestAgentKD(SimpleAgent nearestAgent)
	{
		this.nearestAgent = nearestAgent;
	}
	
}
