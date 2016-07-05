package jcompute.gui.view.renderer.shapes;

import jcompute.gui.view.renderer.ViewRendererInf;
import jcompute.gui.view.renderer.util.VectorGraphic2d;
import jcompute.math.geom.JCCircle;

public class CircleCrossHair
{
	private final JCCircle circle;
	
	public CircleCrossHair(float cx, float cy, float size)
	{
		circle = new JCCircle(cx, cy, size);
	}
	
	public void set(float x, float y, float size)
	{
		circle.center.x = x;
		circle.center.y = y;
		circle.radius = size;
	}
	
	public void setPosition(float x, float y)
	{
		circle.center.x = x;
		circle.center.y = y;
	}
	
	public float getX()
	{
		return circle.center.x;
	}
	
	public float getY()
	{
		return circle.center.y;
	}
	
	public float getSize()
	{
		return circle.radius;
	}
	
	public void render(ViewRendererInf renderer)
	{
		VectorGraphic2d.CircleFilled(renderer, circle.center.x, circle.center.y, circle.radius, 1f, 1f, 1f, 0.15f);
		VectorGraphic2d.CircleOutline(renderer, circle.center.x, circle.center.y, circle.radius, 1f, 1f, 1f, 1f, 0.5f);
	}
}
