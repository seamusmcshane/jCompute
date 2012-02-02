package alife;
import java.io.File;
import java.util.Random;

import org.lwjgl.LWJGLUtil;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.MouseListener;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;

public class mainApp extends BasicGame implements MouseListener
{
		
	static int screen_width=700;
	static int screen_height=700;
	static int frame_rate=30;
	static boolean frame_cap=true;
	static int frame_num=0;
	static int step_num=0;
	
	int num_agents=1000;

	AgentManager agentManager;
	
	World world;
	static int world_size=screen_height-20;
	int agent_size=5;
	
	int draw=0;
	
	public static Vector2f mouse_pos = new Vector2f(world_size,world_size);
	private static int mouse_range=25;
	public static Rectangle m = new Rectangle(mouse_pos.getX()-mouse_range,mouse_pos.getY()-mouse_range,mouse_range*2,mouse_range*2);

	
	public static Vector2f global_translate = new Vector2f(10,10);
	
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
	    
	    agentManager = new AgentManager(num_agents);
	    
		int i;

		int x,y,t,s;
		for(i=0;i<num_agents;i++)
		{
			//xr.setSeed(System.currentTimeMillis());
			
			x=xr.nextInt(world_size)+1;
			
		   // yr.setSeed(System.currentTimeMillis());
		    
		    y=yr.nextInt(world_size)+1;
		    
		    t=tr.nextInt(2)+1;
		    
		    s=sr.nextInt(agent_size)+4;

		    
			//agent[i] = new SimpleAgent(x,y,agent_size,world_size);
		    
		    agentManager.addNewAgent(new SimpleAgent(i,x,y,s,t));
		    
		    
		}  
	    
	    
		
	}

	@Override
	public void update(GameContainer container, int delta) throws SlickException
	{
		
		/* Populate the Agent Set */
		
		/* Randomize Set Order */
		
		/* While Set not Empty - Pick Agent - Do Agent Action */
		
		agentManager.doAi();
		
		step_num++;		

	}

	@Override
	public void render(GameContainer container, Graphics g) throws SlickException
	{
		
		/* Center Origin on Screen */
		g.translate(global_translate.getX(),global_translate.getY());
		g.setClip(0,0,screen_width,screen_height);
		
		g.setAntiAlias(true);
						
		/*Grid */
		world.drawWorld(g);
				
		/* Agents */
		drawAgents(g);

		g.setColor(Color.blue);
		g.draw(m);

		/* Gui */
		g.setColor(Color.white);
		g.drawString("Alife Sim Test\nNumber Active Agents : " + num_agents + "\nMouse Pos :"+ mouse_pos.toString() + "\n Frame Number : " + frame_num + "\n Step Num : " + step_num, screen_width/4, 0);		

		frame_num++;
		
	}

	private void drawAgents(Graphics g)
	{		
		agentManager.drawAI(g);
	}
	
	@Override
	public void mouseMoved(int oldx, int oldy, int newx, int newy) 
	{
		mouse_pos.set(newx-global_translate.getX(), newy-global_translate.getY());
		m.setLocation(mouse_pos.getX()-mouse_range,mouse_pos.getY()-mouse_range);

	}
	
	public static void main(String[] args)
	{
		try
		{
			/* - For standalone build */
			//System.setProperty("org.lwjgl.librarypath", new File(new File(System.getProperty("user.dir"), "native"),LWJGLUtil.PLATFORM_WINDOWS_NAME).getAbsolutePath());
			//System.setProperty("net.java.games.input.librarypath", System.getProperty("org.lwjgl.librarypath"));
			
			AppGameContainer app = new AppGameContainer(new mainApp());
			app.setTitle("Alife Sim");
			app.setUpdateOnlyWhenVisible(false);
			app.setDisplayMode(screen_width, screen_height, false);
			
			app.setClearEachFrame(true);
			app.setVSync(false);
			
			if(frame_cap)
			{
				app.setTargetFrameRate(frame_rate);
			}
			
	        app.start();
		}
		catch (SlickException e)
		{
			e.printStackTrace();
		}
	}
}