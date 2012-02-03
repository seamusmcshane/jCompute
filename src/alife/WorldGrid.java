package alife;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Line;

public class WorldGrid
{

	private Line[] hlines;
	private Line[] vlines;
	
	int num;
	int size;
	int step;
	
	public WorldGrid(int size,int step)
	{
		this.size=size;
		this.step = step;
		generateGrid();
	}
	
	private void generateGrid()
	{
		num=1+(size/step); /* size = 100 then lines every 10 */
		
		hlines = new Line[num];
		vlines = new Line[num];
		
		int i=0;
		int x=0;
		
		for(i=0;i<num;i++)
		{			
			hlines[i] = new Line(x,0,x, size);			
			vlines[i] = new Line(0,x,size,x);
			x=x+step;
		}		
	}
	
	public void drawGrid(Graphics g)
	{
		/* Center Grid */
		g.translate(-size/2, -size/2);
		int col=0;
		int i=0;
		for(i=0;i<num;i++)
		{		
			g.setColor(new Color(col+(int)(mainApp.glocal_scale*100),col+(int)(mainApp.glocal_scale*100),col+(int)(mainApp.glocal_scale*100)));
			g.draw(hlines[i]);
			g.draw(vlines[i]);
		}		
		
		//g.resetTransform();
	}
	
}
