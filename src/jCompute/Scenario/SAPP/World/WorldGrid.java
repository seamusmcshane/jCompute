package jCompute.Scenario.SAPP.World;

import jCompute.Gui.View.GUISimulationView;
import jCompute.Gui.View.Graphics.A2DLine;
import jCompute.Gui.View.Graphics.A2RGBA;

import java.util.ArrayList;

/**
 * This class contains the methods needed to create 
 * and draw the background grid for the world.
 */
public class WorldGrid
{
	private ArrayList<A2DLine> standardGridLines;
	private ArrayList<A2DLine> majorGridLines;
	
	/**
	 * Instantiates a new world grid.
	 *
	 * @param size the size
	 * @param step the step
	 */
	public WorldGrid(int size, int step)
	{
		generateGrid(size,step);
	}

	/** 
	 * Grid Generation 
	 */
	private void generateGrid(int size, int step)
	{
		
		///If step = 8 then this will be every 64 lines 
		int major_div = step * step; 

		// ie if size = 1024 then lines every 8
		int standardLineNum = size / step; 
		
		// The major ines
		int majorLineNum = size / major_div;
		
		// Stores horz and vert
		standardGridLines	= new ArrayList<A2DLine>(standardLineNum*2);
		majorGridLines	= new ArrayList<A2DLine>(majorLineNum*2);
		
		A2RGBA major = new A2RGBA(0.2f,0.2f,0.2f,1f);
		A2RGBA minor = new A2RGBA(0.1f,0.1f,0.1f,1f);
		
		int pos = 0;
		for (int i = 0; i < standardLineNum; i++)
		{					
			if (i % major_div == 0)
			{				
				majorGridLines.add(new A2DLine(pos, 0, pos, size,major));
				majorGridLines.add(new A2DLine(0, pos, size, pos,major));
			}
			else
			{
				standardGridLines.add(new A2DLine(pos, 0, pos, size,minor));
				standardGridLines.add(new A2DLine(0, pos, size, pos,minor));
			}
			pos = pos + step;
		}
	}

	/** 
	 * Draws the grid method on the image object 
	 * @param GUISimulationView simView
	 */
	public void draw(GUISimulationView simView)
	{	
		for(A2DLine line : standardGridLines)
		{
			simView.drawLine(line,line.getColor(),2f,false);
		}
		for(A2DLine line : majorGridLines)
		{
			simView.drawLine(line,line.getColor(),2f,false);
		}
	}

}
