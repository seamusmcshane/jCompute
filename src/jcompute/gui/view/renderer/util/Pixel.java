package jcompute.gui.view.renderer.util;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import jcompute.gui.view.renderer.ViewRendererInf;

public class Pixel
{
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
}
