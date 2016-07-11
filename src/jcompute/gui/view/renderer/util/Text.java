package jcompute.gui.view.renderer.util;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import jcompute.gui.view.renderer.ViewRendererInf;

public class Text
{
	public static void String(ViewRendererInf ren, BitmapFont font, float x, float y, String text)
	{
		SpriteBatch sb = ren.getSpriteBatch();
		
		sb.begin();
		font.draw(sb, text, x, y);
		sb.end();
	}
	
	public static void String(ViewRendererInf ren, BitmapFont font, float x, float y, float r, float g, float b, float a, String text)
	{
		SpriteBatch sb = ren.getSpriteBatch();
		
		sb.begin();
		font.setColor(r, g, b, a);
		font.draw(sb, text, x, y);
		sb.end();
	}
}
