package testingApp;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Line;

/*
 * World Grid
 * This class contains the methods needed to create 
 * and draw the background grid for the world.
 * - No external interactions other than draw
 */ 
public class WorldGrid
{
	private Line[] hlines;
	private Line[] vlines;
	
	private int num;
	private int size;
	private int step;
	private final int major_div=50; /* TODO make Adjustable */
	
	public WorldGrid(int size,int step)
	{
		this.size=size;
		this.step = step;
		generateGrid();
	}
	
	/* Generation Method */
	private void generateGrid()
	{
		num=1+(size/step); /* ie if size = 1000 then lines every 10 and Major divs every 50 */
		
		hlines = new Line[num];
		vlines = new Line[num];
		
		int x=0;		
		for(int i=0;i<num;i++)
		{			
			hlines[i] = new Line(x,0,x, size);			
			vlines[i] = new Line(0,x,size,x);
			x=x+step;
		}		
	}
	
	/* Draw method for the grid */
	public void drawGrid(Graphics g)
	{					
			g.setColor(new Color(25,25,25));
			for(int i=0;i<num;i++)
			{					
				if(i%major_div == 0)
				{
					g.setColor(new Color(50,50,50));	/* Major Div */
				}
				else
				{
					g.setColor(new Color(25,25,25));	/* Normal Line */
				}
				g.draw(hlines[i]);
				g.draw(vlines[i]);
			}		
	}

}
	

