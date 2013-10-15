package alifeSim.Gui;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

import alifeSim.Alife.SimpleAgent.SimpleAgentEnum.AgentType;

public class StatsDensityPanel extends JPanel
{
	int [][][] grid;	
	int resolution=0;
	int worldSize=0;
	int divisorStep=0;

	private int graphWidth;
	private int graphHeight;
	private int cellWidth;
	private int cellHeight;
	
	private int predMax;
	private int preyMax;
	private int ColorBoost = 128;
	
	public StatsDensityPanel(int resolution,int worldSize)
	{
		this.setDoubleBuffered(true);
		setUpDensityPanel(resolution,worldSize);
	}
	
	public void setUpDensityPanel(int resolution,int worldSize)
	{
		this.worldSize = worldSize;
		this.resolution = resolution;
		
		initGrid();
		
		calculateDivisor();
	}
	
	public void resetStats()
	{
		initGrid();
		
		calculateDivisor();
	}
	
	/* Assumes Square World */
	private void calculateDivisor()
	{
		divisorStep = worldSize/resolution;
		
		System.out.println("worldSize : " + worldSize);

		System.out.println("resolution : " + resolution);

		System.out.println("divisorStep : " + divisorStep);

	}
	
	private void initGrid()
	{
		
		grid = new int [resolution][resolution][2];
		
		int y=0;
		int x=0;
		
		for(y=0;y<resolution;y++)
		{
			for(x=0;x<resolution;x++)
			{
				grid[y][x][0] = 0;	// Prey no
				grid[y][x][1] = 0;  // Pred
			}
		}
	}	
	
	public void incrementAgentNum(float fx,float fy,AgentType type)
	{
		//System.out.println("fx : " + fx);
		//System.out.println("fy : " + fy);
		
		int ix= (int)fx;
		int iy= (int)fy;
		
		//System.out.println("ix : " + ix);
		//System.out.println("iy : " + iy);	
		
		int ax = ix/divisorStep;
		int ay = iy/divisorStep;
		
		if(ax == resolution)
		{
			ax = resolution-1;
		}
		if(ay == resolution)
		{
			ay = resolution-1;
		}
		
		/*System.out.println("divisorStep : " + divisorStep);
		System.out.println("ax : " + ax);
		System.out.println("ay : " + ay);*/
		
		if(type == AgentType.PREY)
		{
			grid[ax][ay][0]++;	
		}
		else
		{
			grid[ax][ay][1]++;	
		}
	}
		
	/* Todo */
	public void drawGrid(Graphics g)
	{
		int y=0;
		int x=0;
		
		for(y=0;y<resolution;y++)
		{
			for(x=0;x<resolution;x++)
			{

				if ( (grid[y][x][0] > 0 ))
				{
					int preyalpha = ((grid[y][x][0])*(255/ ((preyMax/100)+1) ));
					if(preyalpha>255)
					{
						preyalpha = 255;
					}
					g.setColor(new Color(0,0, 255,preyalpha));

					g.fillRect(y*cellWidth, x*cellHeight, (1+y)*cellWidth, (1+x)*cellHeight);

				}
				else if((grid[y][x][1] > 0 ))
				{
					int predalpha = ((grid[y][x][1])*(255/ ((predMax/100)+1)) );
					if(predalpha>255)
					{
						predalpha = 255;
					}
					g.setColor(new Color( 255,0,0,predalpha));
				
					g.fillRect(y*cellWidth, x*cellHeight, (1+y)*cellWidth, (1+x)*cellHeight);
				}
				else
				{
					g.setColor(Color.DARK_GRAY);
					g.fillRect(y*cellWidth, x*cellHeight, (1+y)*cellWidth, (1+x)*cellHeight);
				}
				
				g.setColor(Color.black);
				g.drawRect(y*cellWidth, x*cellHeight, (1+y)*cellWidth, (1+x)*cellHeight);	
			}
		}
	}
	
	/** Draws the graph 
	 * @param g Graphics
	 */
	public void paintComponent(Graphics g)
	{
		g.clearRect(this.getX(),this.getY(),this.getWidth(),this.getHeight());
		super.paintComponent(g);
		
		calculateGraphSize();
		
		drawGrid(g);
		
	}
	
	
	/** Gets the widths and height of this panel */
	private void calculateGraphSize()
	{
		graphWidth = this.getWidth() - 1;
		graphHeight = this.getHeight() - 2;
		
		cellWidth = graphWidth / resolution;
		cellHeight = graphHeight / resolution;

	}

	public void drawGraph()
	{
		this.repaint();		
	}

	public void resetAgentStats()
	{
		int y=0;
		int x=0;
		
		for(y=0;y<resolution;y++)
		{
			for(x=0;x<resolution;x++)
			{
				//grid[y][x][0] = 0;	// Plant no
				grid[y][x][0] = 0;  // Prey
				grid[y][x][1] = 0;  // Predators	
			}
		}
		
	}
	
	
	public void setPredMax(int pred)
	{
		this.predMax = pred;
	}
	
	public void setPreyMax(int prey)
	{
		this.preyMax = prey;
	}
	
}
