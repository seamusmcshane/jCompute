package jcompute.gui.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;

import jcompute.gui.view.graphics.A2DCircle;
import jcompute.gui.view.graphics.A2DLine;
import jcompute.gui.view.graphics.A2DRectangle;
import jcompute.gui.view.graphics.A2DVector2f;
import jcompute.gui.view.graphics.A2RGBA;

public class Lib2D
{
	private static final float DEFAULT_LINE_WIDTH = 0.10f;
	
	/*
	 * ***************************************************************************************************
	 * Text
	 *****************************************************************************************************/
	
	public static void drawText(ViewRendererInf ren, float x, float y, String text)
	{
		SpriteBatch sb = ren.getSpriteBatch();
		BitmapFont font = ren.getFont();
		
		sb.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		sb.begin();
		font.draw(sb, text, x, y);
		sb.end();
	}
	
	public static void drawText(ViewRendererInf ren, float x, float y, float r, float g, float b, float a, String text)
	{
		SpriteBatch sb = ren.getSpriteBatch();
		BitmapFont font = ren.getFont();
		
		if(a < 1)
		{
			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		}
		
		sb.begin();
		font.setColor(r, g, b, a);
		font.draw(sb, text, x, y);
		sb.end();
		
		if(a < 1)
		{
			Gdx.gl.glDisable(GL20.GL_BLEND);
		}
	}
	
	/*
	 * ***************************************************************************************************
	 * PixelMap
	 *****************************************************************************************************/
	public static float drawPixelMap(ViewRendererInf ren, int id, float x, float y, boolean noFilter1to1)
	{
		SpriteBatch sb = ren.getSpriteBatch();
		Pixmap pTemp = ren.getPixmap(id);
		Texture tTemp = ren.getTexture(id);
		int textureSize = ren.getTextureSize(id);
		
		tTemp.draw(pTemp, 0, 0);
		
		float min = Math.min(ren.getWidth(), ren.getHeight());
		
		float scale = min / textureSize;
		
		if(noFilter1to1)
		{
			float mod = Math.round(scale);
			
			// Avoid scaling within 10percent of an exact multiple of texture size - also disable filtering
			if(scale < (mod * 1.1f) && scale > (mod * 0.9f))
			{
				scale = mod;
				
				tTemp.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
			}
			else
			{
				tTemp.setFilter(TextureFilter.Linear, TextureFilter.Linear);
			}
		}
		
		int textureScale = (int) (textureSize * scale);
		
		sb.begin();
		
		// Positions the resized texture in the centre of the display.
		sb.draw(tTemp, x - (textureScale / 2), y - (textureScale / 2), textureScale, textureScale);
		
		sb.end();
		
		return textureScale;
	}
	
	/*
	 * ***************************************************************************************************
	 * Texture
	 *****************************************************************************************************/
	public static void drawTexture(ViewRendererInf ren, Texture texture, float x, float y, float width, float height, float rows, float cols)
	{
		SpriteBatch sb = ren.getSpriteBatch();
		
		sb.begin();
		
		sb.draw(texture, x, y, width, height);
		
		for(int r = 0; r < rows; r++)
		{
			for(int c = 0; c < cols; c++)
			{
				sb.draw(texture, x + (width * c), y + (height * r), width, height);
			}
		}
		
		sb.end();
	}
	
	/*
	 * ***************************************************************************************************
	 * Outline Circle
	 *****************************************************************************************************/
	public static void drawCircle(ViewRendererInf ren, float[] pos, float radius, A2RGBA color, float lineWidth)
	{
		drawCircle(ren, pos[0], pos[1], radius, color, lineWidth);
	}
	
	public static void drawCircle(ViewRendererInf ren, float x, float y, float radius, A2RGBA color, float lineWidth)
	{
		drawCircle(ren, x, y, radius, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha(), lineWidth);
	}
	
	public static void drawCircle(ViewRendererInf ren, float x, float y, float radius, float r, float g, float b, float a, float lineWidth)
	{
		Camera cam = ren.getCamera();
		
		if(!cam.frustum.pointInFrustum(x, y, 0))
		{
			return;
		}
		
		ShapeRenderer sr = ren.getShapeRenderer();
		
		Gdx.gl20.glLineWidth(lineWidth);
		
		if(a < 1)
		{
			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		}
		
		sr.begin(ShapeType.Line);
		
		sr.setColor(r, g, b, a);
		sr.circle(x, y, radius);
		sr.end();
		
		if(a < 1)
		{
			Gdx.gl.glDisable(GL20.GL_BLEND);
		}
	}
	
	/*
	 * ***************************************************************************************************
	 * Filled Circle
	 *****************************************************************************************************/
	public static void drawFilledCircle(ViewRendererInf ren, float[] pos, float radius, A2RGBA color)
	{
		drawFilledCircle(ren, pos[0], pos[1], radius, color);
	}
	
	public static void drawFilledCircle(ViewRendererInf ren, float x, float y, float radius, A2RGBA color)
	{
		drawFilledCircle(ren, x, y, radius, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
	}
	
	public static void drawFilledCircle(ViewRendererInf ren, float x, float y, float radius, float r, float g, float b, float a)
	{
		Camera cam = ren.getCamera();
		
		if(!cam.frustum.pointInFrustum(x, y, 0))
		{
			return;
		}
		
		if(a < 1)
		{
			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		}
		
		ShapeRenderer sr = ren.getShapeRenderer();
		
		sr.begin(ShapeType.Filled);
		sr.setColor(r, g, b, a);
		sr.circle(x, y, radius);
		sr.end();
		
		if(a < 1)
		{
			Gdx.gl.glDisable(GL20.GL_BLEND);
		}
	}
	
	/*
	 * ***************************************************************************************************
	 * Filled Circle Batch
	 *****************************************************************************************************/
	public static void drawFilledCircleBatch(ViewRendererInf ren, A2DCircle[] circles, A2RGBA[] colors)
	{
		Camera cam = ren.getCamera();
		
		ShapeRenderer sr = ren.getShapeRenderer();
		
		sr.begin(ShapeType.Filled);
		int size = circles.length;
		for(int c = 0; c < size; c++)
		{
			if(cam.frustum.pointInFrustum(circles[c].getX(), circles[c].getY(), 0))
			{
				sr.setColor(colors[c].getRed(), colors[c].getGreen(), colors[c].getBlue(), colors[c].getAlpha());
				sr.circle(circles[c].getX(), circles[c].getY(), circles[c].getRadius());
			}
		}
		
		sr.end();
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
		
		if(color.getAlpha() < 1)
		{
			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		}
		
		sr.begin(ShapeType.Line);
		sr.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
		sr.line(x1, y1, x2, y2);
		sr.end();
		
		Gdx.gl.glDisable(GL20.GL_BLEND);
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
		drawRectangle(ren, rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight(), rectangle.getColor().getRed(), rectangle.getColor()
		.getGreen(), rectangle.getColor().getBlue(), rectangle.getColor().getAlpha(), DEFAULT_LINE_WIDTH);
	}
	
	public static void drawRectangle(ViewRendererInf ren, A2DRectangle rectangle, float lineWidth)
	{
		drawRectangle(ren, rectangle.getX(), rectangle.getY(), rectangle.getWidth(), rectangle.getHeight(), rectangle.getColor().getRed(), rectangle.getColor()
		.getGreen(), rectangle.getColor().getBlue(), rectangle.getColor().getAlpha(), lineWidth);
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
		
		if(a < 1)
		{
			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		}
		
		sr.begin(ShapeType.Line);
		sr.setColor(r, g, b, a);
		sr.rect(x, y, width, height);
		sr.end();
		
		Gdx.gl.glDisable(GL20.GL_BLEND);
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
		
		if(color.getAlpha() < 1)
		{
			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		}
		
		sr.begin(ShapeType.Filled);
		sr.setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
		sr.rect(x, y, width, height);
		sr.end();
		
		Gdx.gl.glDisable(GL20.GL_BLEND);
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
