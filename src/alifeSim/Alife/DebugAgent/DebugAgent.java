package alifeSim.Alife.DebugAgent;

import org.lwjgl.input.Mouse;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;

import alifeSim.Alife.AlifeBody;
import alifeSim.Alife.SimpleAgent.SimpleAgentBody;
import alifeSim.Alife.SimpleAgent.SimpleAgentStats;
import alifeSim.World.World;
import alifeSim.World.WorldInf;

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
					body.setColor(Color.red);
				break;
			case 1 : // TOP RIGHT
					pos[0] = worldSize*0.75;
					pos[1] = worldSize*0.25;
					body.setColor(Color.green);
				break;
			case 2 : // BOTTOM RIGHT
					pos[0] = worldSize*0.75;
					pos[1] = worldSize*0.75;
					body.setColor(Color.orange);
				break;
			case 3 : // BOTTOM LEFT
					pos[0] = worldSize*0.25;
					pos[1] = worldSize*0.75;
					body.setColor(Color.pink);
				break;
			case -1:

					pos[0] = worldSize*0.50; // MIDDLE
					pos[1] = worldSize*0.50;		
					body.setColor(Color.yellow);

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

	public void drawAgent(Graphics g)
	{		
		if(body!=null)
		{
			body.drawTrueBody(g);
		}
	}

}
