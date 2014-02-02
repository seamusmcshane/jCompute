package alifeSim.Alife.DebugAgent;

import org.lwjgl.input.Mouse;

import alifeSim.Alife.AlifeBody;
import alifeSim.Alife.SimpleAgent.SimpleAgentBody;
import alifeSim.Alife.SimpleAgent.SimpleAgentStats;
import alifeSim.Gui.NewSimView;
import alifeSim.World.World;
import alifeSim.World.WorldInf;
import alifeSimGeom.A2RGBA;

public class DebugAgent
{
	private int auto;
	private double pos[];

	private AlifeBody body;
	
	private int worldSize;
	
	private boolean dirUp = true;
	
	private WorldInf world;
	
	public DebugAgent(WorldInf world,int auto, int corner,int worldSize)
	{
		this.world = world;
		
		this.setAuto(auto);
		
		body = new AlifeBody();
		body.setSize(10);
		
		this.worldSize = worldSize;
		
		setPos(corner,worldSize);

	}

	private void setPos(int corner, int worldSize)
	{
		pos = new double[2];
		switch (corner)
		{
			case 0 : // TOP LEFT
					pos[0] = worldSize*0.25;
					pos[1] = worldSize*0.25;
					body.setColor(new A2RGBA(0,1,0,1));
				break;
			case 1 : // TOP RIGHT
					pos[0] = worldSize*0.75;
					pos[1] = worldSize*0.25;
					body.setColor(new A2RGBA(0,1,0,1));
				break;
			case 2 : // BOTTOM RIGHT
					pos[0] = worldSize*0.75;
					pos[1] = worldSize*0.75;
					body.setColor(new A2RGBA(1,1,0,1));
				break;
			case 3 : // BOTTOM LEFT
					pos[0] = worldSize*0.25;
					pos[1] = worldSize*0.75;
					body.setColor(new A2RGBA(0,1,1,1));
				break;
			case -1:
					pos[0] = worldSize*0.50; // MIDDLE
					pos[1] = worldSize*0.50;		
					body.setColor(new A2RGBA(1,0,1,1));

			break;
			default :				
				System.out.println("INVALID CORNER");				
			break;

		}
		
		body.setIntialPos(pos);
	}
	
	public void doMove()
	{
		if(auto == 0)
		{
			if( dirUp)
			{
				if(world.isInvalidPosition((float)pos[0],(float) pos[1]))
				{
					dirUp=false;
				}
			}
			else
			{
				if(world.isInvalidPosition((float)pos[0],(float) pos[1]))
				{
					dirUp=true;
				}
			}
			
			if(dirUp)
			{
				pos[1]--;
			}
			else
			{
				pos[1]++;				
			}
			
			body.setIntialPos(pos);
		}
	}
	
	public double[] getPos()
	{
		return pos;
	}

	public void setPos(double[] pos)
	{
		this.pos = pos;
	}

	public int getAuto()
	{
		return auto;
	}

	public void setAuto(int auto)
	{
		this.auto = auto;
	}

	public void draw(NewSimView simView)
	{		
		if(body!=null)
		{
			body.draw(simView);
		}
	}

}
