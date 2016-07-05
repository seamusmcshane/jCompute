package jcompute.gui.view.renderer.shapes;

import jcompute.gui.view.renderer.ViewRendererInf;
import jcompute.gui.view.renderer.util.VectorGraphic2d;
import jcompute.math.geom.JCCircle;

public class PlusCrossHair
{
	private final JCCircle circle;
	
	public PlusCrossHair(float cx, float cy, float size)
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
		float chX = circle.center.x;
		float chY = circle.center.y;
		float chS = circle.radius;
		float chG = chS * 0.25f;
		
		VectorGraphic2d.Line(renderer, chX - chS, chY, chX - chG, chY, 1f, 0f, 0f, 1f, 1f);
		VectorGraphic2d.Line(renderer, chX + chG, chY, chX + chS, chY, 0f, 1f, 0f, 1f, 1f);
		
		VectorGraphic2d.Line(renderer, chX, chY - chS, chX, chY - chG, 0f, 0f, 1f, 1f, 1f);
		VectorGraphic2d.Line(renderer, chX, chY + chG, chX, chY + chS, 1f, 1f, 0f, 1f, 1f);
	}
}
