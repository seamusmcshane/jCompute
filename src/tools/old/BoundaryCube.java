package tools.old;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;

public class BoundaryCube
{
	private LineStrip3d bottom;
	
	private LineStrip3d l1;
	private LineStrip3d l2;
	private LineStrip3d l3;
	private LineStrip3d l4;
	
	private LineStrip3d top;

	public BoundaryCube(float scale,float r,float g,float b, float a)
	{
		bottom = new LineStrip3d(r,g,b,a);
		float[] bottomPoints = new float[]{-1*scale, -1*scale, -1*scale, 1*scale, -1*scale, -1*scale,1*scale, 1*scale, -1*scale,-1*scale, 1*scale, -1*scale};
		bottom.setPoints(bottomPoints,false);

		
		top = new LineStrip3d(r,g,b,a);
		float[] topPoints = new float[]{-1*scale, -1*scale, 1*scale,1*scale, -1*scale, 1*scale,1*scale, 1*scale, 1*scale,-1*scale, 1*scale, 1*scale };
		top.setPoints(topPoints,false);		
		
		l1 = new LineStrip3d(r,g,b,a);
		
		l1.setPoints(new float[]{-1*scale, -1*scale, -1*scale,-1*scale, -1*scale, 1*scale},false);
				
		l2 = new LineStrip3d(r,g,b,a);
		
		l2.setPoints(new float[]{1*scale, -1*scale, -1*scale,1*scale, -1*scale, 1*scale},false);
		
		l3 = new LineStrip3d(r,g,b,a);
		l3.setPoints(new float[]{1*scale, 1*scale, -1*scale,1*scale, 1*scale, 1*scale},false);
		
		l4 = new LineStrip3d(r,g,b,a);
		l4.setPoints(new float[]{-1*scale, 1*scale, -1*scale,-1*scale, 1*scale, 1*scale},false);		
	}
	
	public void render(Camera cam)
	{
		bottom.render(cam,GL20.GL_LINE_LOOP);
		top.render(cam,GL20.GL_LINE_LOOP);
		l1.render(cam,GL20.GL_LINE_STRIP);
		l2.render(cam,GL20.GL_LINE_STRIP);
		l3.render(cam,GL20.GL_LINE_STRIP);
		l4.render(cam,GL20.GL_LINE_STRIP);
	}
	
	public void dispose()
	{
		bottom.dispose();
		top.dispose();
		l1.dispose();
		l2.dispose();
		l3.dispose();
		l4.dispose();
	}
	
}

