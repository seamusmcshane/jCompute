package alifeSim.World;

import alifeSim.Gui.NewSimView;
import alifeSimGeom.A2DLine;
import alifeSimGeom.A2RGBA;

/**
 * This class contains the methods needed to create 
 * and draw the background grid for the world.
 * - No external interactions other than draw which draws an image of the grid.
 * @author Seamus McShane
 * @version $Revision: 1.0 $
 */
public class WorldGrid
{

	/** The Horizontal Lines. */
	private A2DLine[] hlines;

	/** The Vertical Lines. */
	private A2DLine[] vlines;

	/** Number of lines (generated) */
	private int num;

	/** The size of the grid. */
	private int size;

	/** The step at which to draw each line. */
	private int step;

	/** The division at which to draw major lines. */
	private int major_div; /* TODO make world grid Adjustable */

	/**
	 * Instantiates a new world grid.
	 *
	 * @param size the size
	 * @param step the step
	 */
	public WorldGrid(int size, int step)
	{
		this.size = size;

		this.step = step;

		generateGrid();

	}

	/** 
	 * Generation Method 
	 * Calculates the positions of all the lines.
	 */
	private void generateGrid()
	{
		num = 1 + (size / step); /* ie if size = 1024 then lines every 8 */

		major_div = step * 8; /* If step = 8 then this will be every 64 lines */

		hlines = new A2DLine[num];
		vlines = new A2DLine[num];

		int x = 0;
		for (int i = 0; i < num; i++)
		{
			hlines[i] = new A2DLine(x, 0, x, size);
			vlines[i] = new A2DLine(0, x, size, x);
			x = x + step;
		}

	}

	/** 
	 * Draws the grid method on the image object 
	 * @param g Graphics
	 */
	public void draw(NewSimView simView)
	{
		/*g.setBackground(Color.black);

		g.setColor(new Color(25, 25, 25));

		g.setLineWidth(2f);

		g.setAntiAlias(true);*/
		
		A2RGBA major = new A2RGBA(0.2f,0.2f,0.2f,1f);

		A2RGBA minor = new A2RGBA(0.1f,0.1f,0.1f,1f);
		
		A2RGBA color;

		for (int i = 0; i < num; i++)
		{
			if (i % major_div == 0)
			{
				color = major;
			}
			else
			{
				color = minor;
			}
			simView.drawLine(hlines[i],color,2f);
			simView.drawLine(vlines[i],color,2f);
		}
	}

}
