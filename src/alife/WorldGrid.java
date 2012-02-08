package alife;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Line;

/*
 * World Grid
 * This class contains the methods needed to create 
 * and draw the background grid for the world.
 * - No external interactions other than draw
 */ 
public class WorldGrid
{
	
	Graphics gridGraphics;
	Image gridImage;
	
	/* Grid Lines */
	private Line[] hlines;
	private Line[] vlines;
	
	private int num;
	private int size;
	private int step;
	private int major_div; /* TODO make Adjustable */
	
	public WorldGrid(int size,int step)
	{
		this.size=size;
		this.step = step;
		
		generateGrid();	
		
		setupGridImage();

		setupGridGraphics();
		
		drawGrid();	
	}
	
	/* Generation Method */
	private void generateGrid()
	{
		num=1+(size/step); /* ie if size = 1024 then lines every 8 */
		
		major_div = step*8; /* If step = 8 then this will be every 64 lines */
		
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
	private void drawGrid()
	{					
			gridGraphics.setBackground(Color.black);
		
			gridGraphics.setColor(new Color(25,25,25));
			
			gridGraphics.setLineWidth(2f);
			
			gridGraphics.setAntiAlias(true);
			
			for(int i=0;i<num;i++)
			{					
				if(i%major_div == 0)
				{
					gridGraphics.setColor(new Color(50,50,50));	/* Major Div */
				}
				else
				{
					gridGraphics.setColor(new Color(25,25,25));	/* Normal Line */
				}
				gridGraphics.draw(hlines[i]);
				gridGraphics.draw(vlines[i]);
			}		
	}
	
	private void setupGridImage()
	{
		try
		{
			gridImage = new Image(size,size);
		}
		catch (SlickException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setupGridGraphics()
	{
		try
		{
			gridGraphics = gridImage.getGraphics();
		}
		catch (SlickException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void drawGridImage(Graphics g)
	{
		g.drawImage(gridImage, 0, 0);		
	}
	
}
	

