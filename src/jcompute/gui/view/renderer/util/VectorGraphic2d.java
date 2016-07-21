package jcompute.gui.view.renderer.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import jcompute.gui.view.renderer.ViewRendererInf;

public class VectorGraphic2d
{
	public static void CircleOutline(ViewRendererInf ren, float x, float y, float radius, float r, float g, float b, float a, float lineWidth)
	{
		Camera cam = ren.getCamera();
		
		if(!cam.frustum.pointInFrustum(x, y, 0))
		{
			return;
		}
		
		ShapeRenderer sr = ren.getShapeRenderer();
		
		Gdx.gl20.glLineWidth(lineWidth);
		
		sr.begin(ShapeType.Line);
		
		sr.setColor(r, g, b, a);
		sr.circle(x, y, radius);
		sr.end();
	}
	
	public static void CircleFilled(ViewRendererInf ren, float x, float y, float radius, float r, float g, float b, float a)
	{
		Camera cam = ren.getCamera();
		
		if(!cam.frustum.pointInFrustum(x, y, 0))
		{
			return;
		}
		
		ShapeRenderer sr = ren.getShapeRenderer();
		
		sr.begin(ShapeType.Filled);
		
		sr.setColor(r, g, b, a);
		sr.circle(x, y, radius);
		sr.end();
	}
	
	public static void BatchCircleFilled(ViewRendererInf ren, float[] xyrrgba)
	{
		Camera cam = ren.getCamera();
		
		ShapeRenderer sr = ren.getShapeRenderer();
		
		sr.begin(ShapeType.Filled);
		
		for(int i = 0; i < xyrrgba.length; i += 7)
		{
			
			float x = xyrrgba[i];
			float y = xyrrgba[i + 1];
			
			// TODO Circle
			if(cam.frustum.pointInFrustum(x, y, 0))
			{
				float radius = xyrrgba[i + 2];
				
				float r = xyrrgba[i + 3];
				float g = xyrrgba[i + 4];
				float b = xyrrgba[i + 5];
				float a = xyrrgba[i + 6];
				
				sr.setColor(r, g, b, a);
				sr.circle(x, y, radius);
			}
		}
		sr.end();
	}
	
	public static void RectangleOutline(ViewRendererInf ren, float x, float y, float width, float height, float r, float g, float b, float a, float lineWidth)
	{
		boolean pos00View = true;
		boolean pos01View = true;
		boolean pos11View = true;
		boolean pos10View = true;
		
		Camera cam = ren.getCamera();
		
		if(!cam.frustum.pointInFrustum(x, y, 0))
		{
			pos00View = false;
		}
		
		if(!cam.frustum.pointInFrustum(x + width, y, 0))
		{
			pos10View = false;
		}
		
		if(!cam.frustum.pointInFrustum(x, y + height, 0))
		{
			pos10View = false;
		}
		
		if(!cam.frustum.pointInFrustum(x + width, y + height, 0))
		{
			pos11View = false;
		}
		
		if(!pos00View && !pos10View && !pos01View && !pos11View)
		{
			return;
		}
		
		Gdx.gl20.glLineWidth(lineWidth);
		
		ShapeRenderer sr = ren.getShapeRenderer();
		
		sr.begin(ShapeType.Line);
		
		sr.setColor(r, g, b, a);
		sr.rect(x, y, width, height);
		sr.end();
	}
	
	public static void RectangleFilled(ViewRendererInf ren, float x, float y, float width, float height, float r, float g, float b, float a)
	{
		boolean pos00View = true;
		boolean pos01View = true;
		boolean pos11View = true;
		boolean pos10View = true;
		
		Camera cam = ren.getCamera();
		
		if(!cam.frustum.pointInFrustum(x, y, 0))
		{
			pos00View = false;
		}
		
		if(!cam.frustum.pointInFrustum(x + width, y, 0))
		{
			pos10View = false;
		}
		
		if(!cam.frustum.pointInFrustum(x, y + height, 0))
		{
			pos10View = false;
		}
		
		if(!cam.frustum.pointInFrustum(x + width, y + height, 0))
		{
			pos11View = false;
		}
		
		if(!pos00View && !pos10View && !pos01View && !pos11View)
		{
			return;
		}
		
		ShapeRenderer sr = ren.getShapeRenderer();
		
		sr.begin(ShapeType.Filled);
		
		sr.setColor(r, g, b, a);
		sr.rect(x, y, width, height);
		sr.end();
	}
	
	public static void Line(ViewRendererInf ren, float x1, float y1, float x2, float y2, float r, float g, float b, float a, float width)
	{
		Camera cam = ren.getCamera();
		
		boolean pos1View = true;
		boolean pos2View = true;
		
		if(!cam.frustum.pointInFrustum(x1, y1, 0))
		{
			pos1View = false;
		}
		
		if(!cam.frustum.pointInFrustum(x2, y2, 0))
		{
			pos2View = false;
		}
		
		// TODO not correct - should be line intersects view
		if(!pos1View && !pos2View)
		{
			return;
		}
		
		Gdx.gl20.glLineWidth(width);
		
		ShapeRenderer sr = ren.getShapeRenderer();
		
		sr.begin(ShapeType.Line);
		
		sr.setColor(r, g, b, a);
		sr.line(x1, y1, x2, y2);
		sr.end();
	}
}
