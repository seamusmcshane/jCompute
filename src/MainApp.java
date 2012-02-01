import java.util.Random;

import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.AppGameContainer;

public class mainApp extends BasicGame
{
	static int screen_width=1280;
	static int screen_height=720;
		
	int num_agents=1000;
	//SimpleAgent agent[];
	
	AgentManager agentManager;
	
	World world;
	int world_size=screen_height-20;
	int agent_size=3;
	
	int draw=0;
	
	public mainApp()
	{
		super("MainApp");
	}

	@Override
	public void init(GameContainer container) throws SlickException
	{
		world = new World(world_size);

	    Random xr = new Random();
	    Random yr = new Random();
	    
	    Random tr = new Random();

	    Random sr = new Random();
	    
	    agentManager = new AgentManager();
	    
		int i;

		int x,y,t,s;
		for(i=0;i<=num_agents;i++)
		{
			xr.setSeed(System.nanoTime());
			x=xr.nextInt(world_size)+1;
		    yr.setSeed(System.nanoTime());
		    y=yr.nextInt(world_size)+1;
		    
		    t=tr.nextInt(2)+1;
		    
		    s=sr.nextInt(5)+1;

		    
			//agent[i] = new SimpleAgent(x,y,agent_size,world_size);
		    
		    agentManager.addNewAgent(new SimpleAgent(x,y,s,world_size,t));
		    
		    
		}  
	    
	    
		
	}

	@Override
	public void update(GameContainer container, int delta) throws SlickException
	{
		
		/* Populate the Agent Set */
		
		/* Randomize Set Order */
		
		/* While Set not Empty - Pick Agent - Do Agent Action */
		
				
		agentManager.doAi();
		
		
	}

	@Override
	public void render(GameContainer container, Graphics g) throws SlickException
	{
		
		g.setClip(0,0,screen_width,screen_height);

		/* Center Origin on Screen */
		g.translate(screen_width/4,10);
		g.setAntiAlias(true);
		
		
		/*Grid */
		world.drawWorld(g);
		
		/* Agents */
		drawAgents(g);
		
		/* Gui */
		g.setColor(Color.white);
		g.drawString("Alife Sim Test", screen_height/2, 0);		
	
	}

	private void drawAgents(Graphics g)
	{		
		
		agentManager.drawAI(g);
	
	}
	
	public static void main(String[] args)
	{
		try
		{
			AppGameContainer app = new AppGameContainer(new mainApp());
	        app.setDisplayMode(screen_width, screen_height, false);
			app.setVSync(false);
			//app.setTargetFrameRate(15);
			
	        app.start();
		}
		catch (SlickException e)
		{
			e.printStackTrace();
		}
	}
}