package jCompute.Gui.View;

import jCompute.Gui.View.Graphics.A2DCircle;
import jCompute.Gui.View.Graphics.A2DLine;
import jCompute.Gui.View.Graphics.A2DRectangle;
import jCompute.Gui.View.Graphics.A2DVector2f;
import jCompute.Gui.View.Graphics.A2RGBA;

import java.nio.ByteBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;

public class Lib2D
{
	private static final float DEFAULT_LINE_WIDTH = 0.10f;
	
	public static void drawText(ViewRendererInf ren, float x, float y, String text)
	{
		SpriteBatch sb = ren.getSpriteBatch();
		BitmapFont font = ren.getFont();
		
		sb.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		sb.begin();
		font.draw(sb, text, x, y);
		sb.end();
	}
	
	/*
	 * Text
	 */
	public static void drawText(ViewRendererInf ren, float x, float y, String text, A2RGBA color)
	{
		SpriteBatch sb = ren.getSpriteBatch();
		BitmapFont font = ren.getFont();
		
		sb.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		sb.begin();
		font.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
		font.draw(sb, text, x, y);
		sb.end();
	}
	
	/*
	 * PixelMap
	 */
	public static float drawPixelMap(ViewRendererInf ren, int id, int[] buffer, float x, float y)
	{
		SpriteBatch sb = ren.getSpriteBatch();
		Pixmap pTemp = ren.getPixmap(id);
		Texture tTemp = ren.getTexture(id);
		ByteBuffer pixels = pTemp.getPixels();
		int textureSize = ren.getTextureSize(id);
		
		pixels.asIntBuffer().put(buffer);
		tTemp.draw(pTemp, 0, 0);
		
		float min = Math.min(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		float scale = min / textureSize;
		
		float textureScale = textureSize * scale;
		
		sb.begin();
		
		// Positions the resized texture in the centre of the display.
		sb.draw(tTemp, x - (textureScale / 2), y - (textureScale / 2), textureScale, textureScale);
		
		sb.end();
		
		return textureScale;
	}
	
	/*
	 * Circle
	 */
	public static void drawCircle(ViewRendererInf ren, A2DCircle circle, A2RGBA color)
	{
		Gdx.gl20.glLineWidth(DEFAULT_LINE_WIDTH);
		
		drawCircle(ren, circle, color, DEFAULT_LINE_WIDTH);
	}
	
	public static void drawCircle(ViewRendererInf ren, A2DCircle circle, A2RGBA color, float lineWidth)
	{
		Camera cam = ren.getCamera();
		
		if(!cam.frustum.pointInFrustum(new Vector3(circle.getX(), circle.getY(), 0)))
		{
			return;
		}
		
		ShapeRenderer sr = ren.getShapeRenderer();
		
		Gdx.gl20.glLineWidth(lineWidth);
		
		sr.begin(ShapeType.Line);
		sr.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
		sr.circle(circle.getX(), circle.getY(), circle.getRadius());
		sr.end();
	}
	
	public static void drawCircle(ViewRendererInf ren, float[] pos, float radius, A2RGBA color, float lineWidth)
	{
		Camera cam = ren.getCamera();
		
		if(!cam.frustum.pointInFrustum(new Vector3(pos[0], pos[1], 0)))
		{
			return;
		}
		
		ShapeRenderer sr = ren.getShapeRenderer();
		
		Gdx.gl20.glLineWidth(lineWidth);
		
		sr.begin(ShapeType.Line);
		sr.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
		sr.circle(pos[0], pos[1], radius);
		sr.end();
	}
	
	/*
	 * Filled Circle
	 */
	public static void drawFilledCircle(ViewRendererInf ren, A2DCircle circle, A2RGBA color)
	{
		Camera cam = ren.getCamera();
		
		if(!cam.frustum.pointInFrustum(new Vector3(circle.getX(), circle.getY(), 0)))
		{
			return;
		}
		
		ShapeRenderer sr = ren.getShapeRenderer();
		
		sr.begin(ShapeType.Filled);
		sr.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
		sr.circle(circle.getX(), circle.getY(), circle.getRadius());
		sr.end();
	}
	
	/*
	 * Filled Circle Batch
	 */
	public static void drawFilledCircleBatch(ViewRendererInf ren, A2DCircle[] circles, A2RGBA[] colors)
	{
		Camera cam = ren.getCamera();
		
		ShapeRenderer sr = ren.getShapeRenderer();
		
		sr.begin(ShapeType.Filled);
		int size = circles.length;
		for(int c = 0; c < size; c++)
		{
			if(cam.frustum.pointInFrustum(new Vector3(circles[c].getX(), circles[c].getY(), 0)))
			{
				sr.setColor(colors[c].getRed(), colors[c].getGreen(), colors[c].getBlue(), colors[c].getAlpha());
				sr.circle(circles[c].getX(), circles[c].getY(), circles[c].getRadius());
			}
		}
		
		sr.end();
	}
	
	/*
	 * Transparent Filled Circle
	 */
	public static void drawTransparentFilledCircle(ViewRendererInf ren, A2DCircle circle, A2RGBA color, float transparency)
	{
		Camera cam = ren.getCamera();
		
		if(!cam.frustum.pointInFrustum(new Vector3(circle.getX(), circle.getY(), 0)))
		{
			return;
		}
		
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		ShapeRenderer sr = ren.getShapeRenderer();
		
		sr.begin(ShapeType.Filled);
		sr.setColor(color.getRed(), color.getGreen(), color.getBlue(), transparency);
		sr.circle(circle.getX(), circle.getY(), circle.getRadius());
		sr.end();
		
		Gdx.gl.glDisable(GL20.GL_BLEND);
		
	}
	
	public static void drawTransparentFilledCircle(ViewRendererInf ren, float[] pos, float radius, A2RGBA color, float transparency)
	{
		Camera cam = ren.getCamera();
		
		if(!cam.frustum.pointInFrustum(new Vector3(pos[0], pos[1], 0)))
		{
			return;
		}
		
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		ShapeRenderer sr = ren.getShapeRenderer();
		
		sr.begin(ShapeType.Filled);
		sr.setColor(color.getRed(), color.getGreen(), color.getBlue(), transparency);
		sr.circle(pos[0], pos[1], radius);
		sr.end();
		
		Gdx.gl.glDisable(GL20.GL_BLEND);
	}
	
	/*
	 * Transparent Filled Arc
	 */
	public static void drawTransparentFilledArc(ViewRendererInf ren, float x, float y, float radius, float start, float angle, A2RGBA color)
	{
		Camera cam = ren.getCamera();
		
		if(!cam.frustum.pointInFrustum(new Vector3(x, y, 0)))
		{
			return;
		}
		
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		ShapeRenderer sr = ren.getShapeRenderer();
		
		sr.begin(ShapeType.Filled);
		sr.setColor(color.getRed(), color.getGreen(), color.getBlue(), 0.5f);
		sr.arc(x, y, radius, start, angle);
		sr.end();
		
		Gdx.gl.glDisable(GL20.GL_BLEND);
		
	}
	
	/*
	 * Line
	 */
	public static void drawLine(ViewRendererInf ren, A2DVector2f pos1, A2DVector2f pos2, A2RGBA color, float lineWidth, boolean clipCheck)
	{
		drawLine(ren, pos1.getX(), pos1.getY(), pos2.getX(), pos2.getY(), color, lineWidth, clipCheck);
	}
	
	public static void drawLine(ViewRendererInf ren, A2DLine line, A2RGBA color, float width, boolean clipCheck)
	{
		if(clipCheck)
		{
			Camera cam = ren.getCamera();
			
			boolean pos00View = true;
			boolean pos11View = true;
			
			if(!cam.frustum.pointInFrustum(new Vector3(line.getX1(), line.getY1(), 0)))
			{
				pos00View = false;
			}
			
			if(!cam.frustum.pointInFrustum(new Vector3(line.getX2(), line.getY2(), 0)))
			{
				pos11View = false;
			}
			
			if(!pos00View && !pos11View)
			{
				return;
			}
			
		}
		Gdx.gl20.glLineWidth(width);
		
		ShapeRenderer sr = ren.getShapeRenderer();
		
		sr.begin(ShapeType.Line);
		sr.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
		sr.line(line.getX1(), line.getY1(), line.getX2(), line.getY2());
		sr.end();
		
	}
	
	public static void drawLine(ViewRendererInf ren, float x1, float y1, float x2, float y2, A2RGBA color, float width, boolean clipCheck)
	{
		
		if(clipCheck)
		{
			Camera cam = ren.getCamera();
			
			boolean pos00View = true;
			boolean pos11View = true;
			
			if(!cam.frustum.pointInFrustum(new Vector3(x1, y1, 0)))
			{
				pos00View = false;
			}
			
			if(!cam.frustum.pointInFrustum(new Vector3(x2, y2, 0)))
			{
				pos11View = false;
			}
			
			if(!pos00View && !pos11View)
			{
				return;
			}
		}
		
		Gdx.gl20.glLineWidth(width);
		
		ShapeRenderer sr = ren.getShapeRenderer();
		
		sr.begin(ShapeType.Line);
		sr.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
		sr.line(x1, y1, x2, y2);
		sr.end();
		
	}
	
	public static void drawLine(ViewRendererInf ren, float x1, float y1, float x2, float y2, A2RGBA color, boolean clipCheck)
	{
		if(clipCheck)
		{
			Camera cam = ren.getCamera();
			
			boolean pos00View = true;
			boolean pos11View = true;
			
			if(!cam.frustum.pointInFrustum(new Vector3(x1, y1, 0)))
			{
				pos00View = false;
			}
			
			if(!cam.frustum.pointInFrustum(new Vector3(x2, y2, 0)))
			{
				pos11View = false;
			}
			
			if(!pos00View && !pos11View)
			{
				return;
			}
		}
		
		Gdx.gl20.glLineWidth(DEFAULT_LINE_WIDTH);
		
		ShapeRenderer sr = ren.getShapeRenderer();
		
		sr.begin(ShapeType.Line);
		sr.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
		sr.line(x1, y1, x2, y2);
		sr.end();
		
	}
	
	/*
	 * Line Batch
	 */
	public static void drawLineBatch(ViewRendererInf ren, float[][] vertices, A2RGBA color, float width, boolean clipCheck)
	{
		Gdx.gl20.glLineWidth(width);
		
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		ShapeRenderer sr = ren.getShapeRenderer();
		
		sr.begin(ShapeType.Line);
		sr.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
		
		for(int v = 0; v < vertices.length; v += 2)
		{
			float x1 = vertices[v][0];
			float y1 = vertices[v][1];
			float x2 = vertices[v + 1][0];
			float y2 = vertices[v + 1][1];
			
			if(clipCheck)
			{
				Camera cam = ren.getCamera();
				
				boolean pos00View = true;
				boolean pos11View = true;
				
				if(!cam.frustum.pointInFrustum(new Vector3(x1, y1, 0)))
				{
					pos00View = false;
				}
				
				if(!cam.frustum.pointInFrustum(new Vector3(x2, y2, 0)))
				{
					pos11View = false;
				}
				
				// If either side in view
				if(pos00View || pos11View)
				{
					sr.line(x1, y1, x2, y2);
				}
			}
		}
		
		sr.end();
		Gdx.gl.glDisable(GL20.GL_BLEND);
		
	}
	
	/*
	 * Rectangle
	 */
	public static void drawRectangle(ViewRendererInf ren, A2DRectangle rectangle)
	{
		drawRectangle(ren, rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight(), rectangle.getColor().getRed(), rectangle.getColor().getGreen(),
				rectangle.getColor().getBlue(), rectangle.getColor().getAlpha(), DEFAULT_LINE_WIDTH);
	}
	
	public static void drawRectangle(ViewRendererInf ren, A2DRectangle rectangle, float lineWidth)
	{
		drawRectangle(ren, rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight(), rectangle.getColor().getRed(), rectangle.getColor().getGreen(),
				rectangle.getColor().getBlue(), rectangle.getColor().getAlpha(), lineWidth);
	}
	
	public static void drawRectangle(ViewRendererInf ren, float x, float y, float width, float height, A2RGBA color)
	{
		drawRectangle(ren, x, y, width, height, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha(), DEFAULT_LINE_WIDTH);
	}
	
	public static void drawRectangle(ViewRendererInf ren, float x, float y, float width, float height, A2RGBA color, float lineWidth)
	{
		drawRectangle(ren, x, y, width, height, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha(), lineWidth);
	}
	
	public static void drawRectangle(ViewRendererInf ren, float x, float y, float width, float height, float r, float g, float b, float a)
	{
		drawRectangle(ren, x, y, width, height, r, g, b, a, DEFAULT_LINE_WIDTH);
	}
	
	public static void drawRectangle(ViewRendererInf ren, float x, float y, float width, float height, float r, float g, float b, float a, float lineWidth)
	{
		boolean pos00View = true;
		boolean pos01View = true;
		boolean pos11View = true;
		boolean pos10View = true;
		
		Camera cam = ren.getCamera();
		
		if(!cam.frustum.pointInFrustum(new Vector3(x, y, 0)))
		{
			pos00View = false;
		}
		
		if(!cam.frustum.pointInFrustum(new Vector3(x + width, y, 0)))
		{
			pos10View = false;
		}
		
		if(!cam.frustum.pointInFrustum(new Vector3(x, y + height, 0)))
		{
			pos10View = false;
		}
		
		if(!cam.frustum.pointInFrustum(new Vector3(x + width, y + height, 0)))
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
	
	/*
	 * Filled Rectangle
	 */
	public static void drawFilledRectangle(ViewRendererInf ren, A2DRectangle rectangle, A2RGBA color)
	{
		drawFilledRectangle(ren, rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight(), color);
	}
	
	public static void drawFilledRectangle(ViewRendererInf ren, float x, float y, float width, float height, A2RGBA color)
	{
		boolean pos00View = true;
		boolean pos01View = true;
		boolean pos11View = true;
		boolean pos10View = true;
		
		Camera cam = ren.getCamera();
		
		if(!cam.frustum.pointInFrustum(new Vector3(x, y, 0)))
		{
			pos00View = false;
		}
		
		if(!cam.frustum.pointInFrustum(new Vector3(x + width, y, 0)))
		{
			pos10View = false;
		}
		
		if(!cam.frustum.pointInFrustum(new Vector3(x, y + height, 0)))
		{
			pos10View = false;
		}
		
		if(!cam.frustum.pointInFrustum(new Vector3(x + width, y + height, 0)))
		{
			pos11View = false;
		}
		
		if(!pos00View && !pos10View && !pos01View && !pos11View)
		{
			return;
		}
		
		ShapeRenderer sr = ren.getShapeRenderer();
		
		sr.begin(ShapeType.Filled);
		sr.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
		sr.rect(x, y, width, height);
		sr.end();
	}
	
	/*
	 * Transparent Filled Rectangle
	 */
	public static void drawTransparentFillRectangle(ViewRendererInf ren, float x, float y, float width, float height, A2RGBA color)
	{
		boolean pos00View = true;
		boolean pos01View = true;
		boolean pos11View = true;
		boolean pos10View = true;
		
		Camera cam = ren.getCamera();
		
		if(!cam.frustum.pointInFrustum(new Vector3(x, y, 0)))
		{
			pos00View = false;
		}
		
		if(!cam.frustum.pointInFrustum(new Vector3(x + width, y, 0)))
		{
			pos10View = false;
		}
		
		if(!cam.frustum.pointInFrustum(new Vector3(x, y + height, 0)))
		{
			pos10View = false;
		}
		
		if(!cam.frustum.pointInFrustum(new Vector3(x + width, y + height, 0)))
		{
			pos11View = false;
		}
		
		if(!pos00View && !pos10View && !pos01View && !pos11View)
		{
			return;
		}
		
		ShapeRenderer sr = ren.getShapeRenderer();
		
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		sr.begin(ShapeType.Filled);
		sr.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
		sr.rect(x, y, width, height);
		sr.end();
		
		Gdx.gl.glDisable(GL20.GL_BLEND);
	}
}
