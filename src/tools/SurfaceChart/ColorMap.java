package tools.SurfaceChart;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
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
		
		GlyphLayout layout = new GlyphLayout();
		layout.setText(font, String.valueOf(max));
		
		int fHeight = (int) layout.height;
		int fWidth = (int) layout.width;
		
		FrameBuffer fbo = new FrameBuffer(Format.RGBA8888, width, height, false);
		Pixmap pixmap = new Pixmap(width, height, Format.RGBA8888);
		
		int indent = 60;
		int margin = (int) ((float) 128 * 0.2f);
		int startPad = margin;
		int endPad = margin * 2;
		
		// Background
		pixmap.setColor(0xFFFFFFFF);
		pixmap.fillRectangle(1, 1, width - 1, height - 1);
		
		// Create Color Bar from Palette
		int pval = 0;
		for(int h = 0; h < (height - endPad); h++)
		{
			pval = (int) (((float) h / height) * (pallete.length - 1));
			pixmap.setColor(pallete[pval]);
			pixmap.drawLine(indent + margin, (int) (margin + h), (width - margin), (int) (margin + h));
		}
		
		// Line Border Color bar
		pixmap.setColor(0x000000FF);
		pixmap.drawRectangle(indent + startPad, startPad, (width - endPad - indent), (height - endPad));
		
		// Line Border on Region
		pixmap.drawRectangle(1, 1, width - 1, height - 1);
		
		Texture texture = new Texture(pixmap);
		TextureRegion ct = new TextureRegion(texture, 0, 0, width, height);
		ct.flip(false, false);
		
		Matrix4 pm = new Matrix4();
		pm.setToOrtho2D(0, 0, width, height);
		
		SpriteBatch sb = new SpriteBatch();
		sb.setProjectionMatrix(pm);
		fbo.begin();
		sb.begin();
		
		int minPos = height - ((margin / 2) + (fHeight / 2));
		int midPos = (height / 2) + ((margin / 2) - (fHeight / 2));
		int maxPos = endPad - ((margin / 2) + (fHeight / 2));
		
		sb.draw(ct, 0, 0);
		
		font.getData().setScale(2f);
		font.draw(sb, String.valueOf(min), fWidth / 2, minPos);
		font.draw(sb, String.valueOf(mid), fWidth / 2, midPos);
		font.draw(sb, String.valueOf(max), fWidth / 2, maxPos);
		sb.end();
		fbo.end();
		
		TextureRegion t1 = new TextureRegion(fbo.getColorBufferTexture(), 0, 0, width, height);
		
		t1.flip(false, true);
		
		pixmap.dispose();
		
		return t1;
	}
}
