package alife;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Line;

/**
 * This class contains the methods needed to create 
 * and draw the background grid for the world.
 * - No external interactions other than draw which draws an image of the grid.
 */ 
public class WorldGrid
{
	
	/** The final grid image. */
	private Image gridImage;
	
	/** The grid graphics drawing object. */
	private Graphics gridGraphics;
		
	/** The Horizontal Lines. */
	private Line[] hlines;
	
	/** The Vertical Lines. */
	private Line[] vlines;
	
	/** Number of lines (generated) */
	private int num;
	
	
	/** The size of the grid. */
	private int size;
	
	/** The step at which to draw each line. */
	private int step;
	
	/** The division at which to draw major lines. */
	private int major_div; /* TODO make Adjustable */
	
	/**
	 * Instantiates a new world grid.
	 *
	 * @param size the size
	 * @param step the step
	 */
	public WorldGrid(int size,int step)
	{
		this.size=size;
		
		this.step = step;
		
		generateGrid();	
		
		setupGridImage();

		setupGridGraphics();
		
		drawGridOnImage();	
	}
	
	/** 
	 * Generation Method 
	 * Calculates the positions of all the lines.
	 */
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
	
	/** 
	 * Draws the grid method on the image object 
	 */
	private void drawGridOnImage()
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
	
	/**
	 * Sets up the grid image object.
	 */
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

	/**
	 * Points grid graphics to the image object graphics.
	 */
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

	/**
	 * Draw the grid image on the passed in graphics object.
	 *
	 * @param Graphics g
	 */
	public void drawGridImage(Graphics g)
	{
		g.drawImage(gridImage, 0, 0);		
	}
	
}
	

