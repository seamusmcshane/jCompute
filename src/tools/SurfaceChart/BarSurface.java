package tools.SurfaceChart;

import java.util.Random;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class BarSurface
{
	private ModelBatch modelBatch;
	private Matrix4 sbm4;

	private float barSize;
	
	private ModelInstance instances[][];	
	private HueColorPallete pallete;

	private int columns;

	private BitmapFont axisFont = new BitmapFont();
	private Matrix4 fontm4;

	private int gridSize = 100;
	private SpriteBatch spriteBatch;
	// Base Grid
	private Sprite baseSprite;
	private Texture baseText;
	
	float trans;
	
	public BarSurface(int samples, HueColorPallete pallete)
	{		
		modelBatch = new ModelBatch();

		spriteBatch = new SpriteBatch();
		sbm4 = new Matrix4();

		axisFont = new BitmapFont();
		axisFont.setColor(Color.BLACK);
		fontm4 = new Matrix4();
		
		this.columns = (int) Math.sqrt(samples);
		
		this.barSize = gridSize/columns;

		this.pallete = pallete;
		
		instances = new ModelInstance[columns][columns];
		
		setData(null);
	}
	
	public void setData(float data[][])
	{
		// Pillars
		ModelBuilder modelBuilder = new ModelBuilder();
		
		Random r = new Random();

		float max = barSize*columns/4;
		float crange = pallete.getColorCount()/max;
		trans = columns*barSize/2;

		Model[][] pillars = new Model[columns][columns];		

		float height = 0;
		
		int c=0;
		for(int y=0;y<columns;y++)
		{
			for(int x=0;x<columns;x++)
			{
				height = r.nextFloat()*max;
//				height = (height+5)%max;
				c = (int) (crange * height);
				//c=(c+1)%colorsVal;
				pillars[y][x] = modelBuilder.createBox(barSize,height, barSize,new Material(ColorAttribute.createDiffuse(pallete.getColor(c))),	Usage.Position | Usage.Normal);
				instances[y][x] = new ModelInstance(pillars[y][x]);
				instances[y][x].transform.rotate(1, 0, 0, 90);
				instances[y][x].transform.trn((y*barSize)-trans, (x*barSize)-trans, ((height/2)));
			}
		}
		
	}	
	
	public float getSize()
	{
		return columns * barSize;
	}
	
	public void render(Camera cam,Environment environment)
	{		
		drawGrid(cam,-(100/2),0,0,0,1,0,90);
		drawGrid(cam,0,0,0,0,0,0,90);
		
		drawBars(cam,environment);
		
	}
	
	public void drawBars(Camera cam,Environment environment)
	{
		modelBatch.begin(cam);

		for(int y=0;y<columns;y++)
		{
			for(int x=0;x<columns;x++)
			{
				modelBatch.render(instances[y][x], environment);
			}
		}
		
		modelBatch.end();
	}
	
	public void drawGrid(Camera cam,float x,float y, float z,float axisX, float axisY, float axisZ, float angle)
	{
		ShapeRenderer shapeRenderer = new ShapeRenderer();
		shapeRenderer.setProjectionMatrix(cam.combined);
		shapeRenderer.rotate(axisX, axisY, axisZ, angle);
		shapeRenderer.translate(x, y, z);
		shapeRenderer.setColor(0, 0, 0, 1);
		shapeRenderer.begin(ShapeType.Line);
		for(int yl=0;yl<=gridSize;yl++)
		{
			if(yl%10==0)
			{
				shapeRenderer.line(0-(gridSize/2)-barSize/2, yl-(gridSize/2)-barSize/2, (gridSize/2)-barSize/2, yl-(gridSize/2)-barSize/2);
			}

			for(int xl=0;xl<=gridSize;xl++)
			{
				if(xl%10==0)
				{
					shapeRenderer.line(xl-(gridSize/2)-barSize/2, 0-(gridSize/2)-barSize/2, xl-(gridSize/2)-barSize/2, (gridSize/2)-barSize/2);
				}
			}	
		}
		shapeRenderer.end();
		
	}
	
	public void dispose()
	{
		for(int y=0;y<columns;y++)
		{
			for(int x=0;x<columns;x++)
			{
				instances[y][x].model.dispose();
			}
		}
	}
	
}
