import java.util.Random;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;


public class SimpleAgent
{
	// Agent Body
	private SimpleAgentBody body;
	private int type;
	
	private float world_size;
	
	private float x,y;
	private float x_dir=1,y_dir=1;
	
	private float body_size;
	
	private float next_x,next_y;
	
	/* Movement */
	Random d;
	Vector2f xy;
	private int moves=0;
	private int max_dir=25;
	
	public SimpleAgent(float x,float y,float body_size,float world_size,int type)
	{
		this.body_size=body_size;
		this.world_size=world_size;
		this.x=x;
		this.y=y;		
		
		this.type = type;
		
		d=new Random();
		xy = polarToCar(1,d.nextInt(360));
		
		/* Graphics */
		createBody();	
		
		
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
			//max_dir = d.nextInt(25);

		}
		
		moves++;
		
		this.x=x+xy.getX();
		this.y=y+xy.getY();
		
		body.move(this.x,this.y);
				
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
	private void createBody()
	{
		body = new SimpleAgentBody(x,y,body_size,type);
	}
	
	/*
	 * 
	 * Graphics 
	 * 
	 */
	public void drawAgent(Graphics g)
	{
		body.drawBody(g);
	}

	
	public com.infomatiq.jsi.Rectangle getBodyBounds()
	{
		return body.getBodyBounds();
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
	
	private Vector2f carToPolar(float x,float y)
	{
		float r = (float) Math.sqrt((x*x)+(y*y));
		
		float theta = (float) Math.atan2(y, x);
		
		/* Polar Vector */
		return new Vector2f(r,theta);
	}
	
	/* Debug */
}
