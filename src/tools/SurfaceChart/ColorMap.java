package tools.SurfaceChart;

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
	private HueColorPallete pallete;
	private BitmapFont font;	
	
	public ColorMap(float min, float max, HueColorPallete pallete)
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

		FrameBuffer fbo = new FrameBuffer(Format.RGBA8888,width,height, false);		
		Pixmap pixmap = new Pixmap(width,height, Format.RGBA8888 );

		// Create Color Bar
		float percentage = 0;
		float cStart = height*0.1f;
		float cEnd = height *0.9f;
		
		for(int h=0;h<height;h++)
		{
			percentage = ((float)h/(float)height)*100f;
			pixmap.setColor(pallete.getPercentageColor(percentage));
			pixmap.drawLine(fWidth*3, (int)(cStart+h), (width-1), (int)(cStart+h));
		}
		Texture texture = new Texture(pixmap);
		TextureRegion ct = new TextureRegion(texture,0,0,width,height);
		ct.flip(false, true);
		
		Matrix4 pm = new Matrix4();
		pm.setToOrtho2D(0, 0, width, height);
		
		SpriteBatch sb = new SpriteBatch();
		sb.setProjectionMatrix(pm);
			fbo.begin();
				sb.begin();

				sb.draw(ct, 0, 0);
				font.scale(1f);
				font.draw(sb, String.valueOf(min), 0f,cStart+fHeight);
				font.draw(sb, String.valueOf(mid), 0f,(cEnd/2f)+cStart+fHeight);
				font.draw(sb, String.valueOf(max), 0f,cEnd+cStart+fHeight);
				
				sb.end();
			fbo.end();		
		
		TextureRegion t1 = new TextureRegion(fbo.getColorBufferTexture(),0,0,width,height);
		
		t1.flip(false, true);
		
		pixmap.dispose();
		
		return t1;
	}
}
