package tools.SurfaceChart;

import jCompute.Gui.View.Misc.Pallete;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;

public class ColorMap
{
	private float min = 0f;
	private float mid = 50f;
	private float max = 100f;
	private int[] pallete;
	private BitmapFont font;
	
	public ColorMap(float min, float max, int[] pallete)
	{
		this.min = min;
		this.max = max;
		this.pallete = pallete;
	}
	
	public TextureRegion getTextureRegion()
	{
		int width = 128;
		int height = 1024;
		
		font = new BitmapFont();
		font.setColor(Color.BLACK);
		int fHeight = (int) font.getBounds(String.valueOf(max)).height;
		int fWidth = (int) font.getBounds(String.valueOf(max)).width;
		
		FrameBuffer fbo = new FrameBuffer(Format.RGBA8888, width, height, false);
		Pixmap pixmap = new Pixmap(width, height, Format.RGBA8888);
		
		int indent = 30;
		int margin = (int) ((float)128*0.2f);
		int startPad = margin;
		int endPad = margin*2;
		
		// Create Color Bar
		int pval = 0;
		for(int h = 0; h < (height-endPad); h++)
		{
			pval = (int) (((float) h / height) * (Pallete.PALETTE_SIZE - 1));
			pixmap.setColor(pallete[pval]);
			pixmap.drawLine(indent+margin, (int) (margin + h), (width-margin), (int) (margin + h));
		}
		
		pixmap.setColor(0x000000FF);
		pixmap.drawRectangle(indent+startPad,startPad, (width-endPad-indent),(height-endPad));
		
		//pixmap.setColor(0xFF0000FF);
		pixmap.drawRectangle(1,1, width-1,height-1);
		
		Texture texture = new Texture(pixmap);
		TextureRegion ct = new TextureRegion(texture, 0, 0, width, height);
		ct.flip(false, false);
		
		Matrix4 pm = new Matrix4();
		pm.setToOrtho2D(0, 0, width, height);
		
		SpriteBatch sb = new SpriteBatch();
		sb.setProjectionMatrix(pm);
		fbo.begin();
		sb.begin();
		
		int minPos =  height - ((margin/2)+(fHeight/2));
		int midPos =  (height/2) - ((margin/2)+(fHeight/2));
		int maxPos =  endPad - ((margin/2)+(fHeight/2));
		
		sb.draw(ct, 0, 0);
		font.scale(0.25f);
		font.draw(sb, String.valueOf(min), fWidth/2, minPos);
		font.draw(sb, String.valueOf(mid),fWidth/2, midPos);
		font.draw(sb, String.valueOf(max), fWidth/2, maxPos);
		sb.end();
		fbo.end();
		
		TextureRegion t1 = new TextureRegion(fbo.getColorBufferTexture(), 0, 0, width, height);
		
		t1.flip(false, true);
		
		pixmap.dispose();
		
		return t1;
	}
}
